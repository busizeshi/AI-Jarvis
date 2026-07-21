package com.notegather.biz.knowledge.application.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.notegather.biz.knowledge.application.dto.NoteTreeNode;
import com.notegather.biz.knowledge.domain.enums.LibraryType;
import com.notegather.biz.knowledge.domain.enums.NodeType;
import com.notegather.biz.knowledge.domain.enums.NoteType;
import com.notegather.biz.knowledge.domain.model.Library;
import com.notegather.biz.knowledge.domain.model.Note;
import com.notegather.biz.knowledge.domain.model.NoteVersion;
import com.notegather.biz.knowledge.domain.repository.LibraryRepository;
import com.notegather.biz.knowledge.domain.repository.NoteRepository;
import com.notegather.biz.knowledge.infrastructure.persistence.note.mapper.NoteVersionMapper;
import com.notegather.biz.knowledge.infrastructure.messaging.NoteGraphProjectionPublisher;
import com.notegather.biz.knowledge.infrastructure.messaging.NoteFlashcardPublisher;
import com.notegather.biz.knowledge.infrastructure.messaging.NoteContentIndexPublisher;
import com.notegather.common.api.knowledge.dto.NoteSummaryDTO;
import com.notegather.common.api.collaboration.CollaborationPermissionFacade;
import com.notegather.common.api.collaboration.PermissionAction;
import com.notegather.common.api.collaboration.ResourceType;
import com.notegather.common.api.collaboration.dto.ResourceDescriptorDTO;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.dubbo.config.annotation.DubboReference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private static final int MAX_NODE_DEPTH = 5;
    private final LibraryRepository libraryRepository;
    private final NoteRepository noteRepository;
    private final NoteVersionMapper noteVersionMapper;
    private final NoteLinkService noteLinkService;
    private final NoteGraphProjectionPublisher noteGraphProjectionPublisher;
    private final NoteFlashcardPublisher noteFlashcardPublisher;
    private final NoteContentIndexPublisher noteContentIndexPublisher;

    @DubboReference(check = false)
    private CollaborationPermissionFacade collaborationPermissionFacade;

    public List<Library> listLibraries(Long userId) {
        List<Library> libraries = new ArrayList<>(libraryRepository.findByUserId(userId, false));
        if (collaborationPermissionFacade == null) return libraries;
        for (Long libraryId : collaborationPermissionFacade.listAccessibleLibraryIds(userId)) {
            Library library = libraryRepository.findById(libraryId);
            if (library != null && libraries.stream().noneMatch(item -> item.getId().equals(libraryId))) {
                libraries.add(library);
            }
        }
        return libraries;
    }

    public Library getLibrary(Long userId, Long libraryId) {
        return requireLibrary(userId, libraryId, PermissionAction.READ);
    }

    public List<Library> recycleLibraries(Long userId) {
        return libraryRepository.findByUserId(userId, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public Library createLibrary(Long userId, String name, LibraryType type, String description, Integer sortOrder) {
        Library library = new Library();
        library.setUserId(userId);
        library.setName(name.trim());
        library.setType(type.name());
        library.setDescription(description);
        library.setSortOrder(sortOrder == null ? 0 : sortOrder);
        libraryRepository.save(library);
        return library;
    }

    @Transactional(rollbackFor = Exception.class)
    public Library updateLibrary(Long userId, Long libraryId, String name, LibraryType type, String description, Integer sortOrder) {
        Library library = requireLibrary(userId, libraryId, PermissionAction.MANAGE);
        if (name != null) {
            if (StrUtil.isBlank(name)) throw new BusinessException(ResultCode.BAD_REQUEST, "知识库名称不能为空");
            library.setName(name.trim());
        }
        if (type != null) library.setType(type.name());
        if (description != null) library.setDescription(description);
        if (sortOrder != null) library.setSortOrder(sortOrder);
        libraryRepository.update(library, library.getUserId());
        return library;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteLibrary(Long userId, Long libraryId, String confirmationName) {
        Library library = requireLibrary(userId, libraryId, PermissionAction.MANAGE);
        if (!Objects.equals(userId, library.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只有知识库所有者可以删除知识库");
        }
        if (!Objects.equals(library.getName(), StrUtil.trim(confirmationName))) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请输入完整的知识库名称以确认删除");
        }
        List<Note> nodes = noteRepository.findByLibraryId(libraryId, library.getUserId(), false);
        for (Note note : nodes) {
            if (note.getParentId() == null) {
                deleteSubtree(library.getUserId(), note.getId(), nodes);
            }
        }
        libraryRepository.delete(libraryId, library.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void restoreLibrary(Long userId, Long libraryId) {
        if (libraryRepository.findDeletedByIdAndUserId(libraryId, userId) == null || !libraryRepository.restore(libraryId, userId)) {
            throw new BusinessException(ResultCode.LIBRARY_NOT_FOUND);
        }
    }

    public List<NoteTreeNode> tree(Long userId, Long libraryId) {
        Library library = requireLibrary(userId, libraryId, PermissionAction.READ);
        Map<Long, List<NoteTreeNode>> children = new HashMap<>();
        List<NoteTreeNode> roots = new ArrayList<>();
        for (Note note : noteRepository.findByLibraryId(libraryId, library.getUserId(), false)) {
            NoteTreeNode node = NoteTreeNode.from(note);
            children.computeIfAbsent(note.getParentId(), ignored -> new ArrayList<>()).add(node);
        }
        attachChildren(null, roots, children);
        return roots;
    }

    @Transactional(rollbackFor = Exception.class)
    public Note createNote(Long userId, Long libraryId, Long parentId, NodeType nodeType, String title,
                           NoteType noteType, String content, Integer sortOrder) {
        Library library = requireLibrary(userId, libraryId, PermissionAction.EDIT);
        List<Note> nodes = noteRepository.findByLibraryId(libraryId, library.getUserId(), false);
        Note parent = requireParent(nodes, libraryId, parentId);
        if (nodeType == NodeType.NOTE && noteType == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "笔记类型不能为空");
        }
        if (nodeType == NodeType.FOLDER && noteType != null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件夹不能设置笔记类型");
        }
        if (depth(parent, nodes) + 1 > MAX_NODE_DEPTH) {
            throw new BusinessException(ResultCode.FOLDER_MAX_DEPTH);
        }
        Note note = new Note();
        note.setUserId(library.getUserId());
        note.setLibraryId(libraryId);
        note.setParentId(parentId);
        note.setNodeType(nodeType.name());
        note.setTitle(title.trim());
        note.setNoteType(noteType == null ? null : noteType.name());
        note.setContent(nodeType == NodeType.FOLDER ? "" : Objects.requireNonNullElse(content, ""));
        note.setSortOrder(sortOrder == null ? 0 : sortOrder);
        note.setVersion(1);
        note.setParseStatus(nodeType == NodeType.NOTE && StrUtil.isNotBlank(note.getContent()) ? "PENDING" : "NONE");
        noteRepository.save(note);
        if (nodeType == NodeType.NOTE) {
            noteLinkService.refreshLibraryLinks(library.getUserId(), libraryId);
            if (StrUtil.isNotBlank(note.getContent())) {
                if (!noteContentIndexPublisher.publishUpdated(note)) {
                    note.setParseStatus("FAILED");
                    noteRepository.update(note, note.getUserId());
                }
            }
        }
        return note;
    }

    public Note getNote(Long userId, Long noteId) {
        return requireNote(userId, noteId, PermissionAction.READ);
    }

    @Transactional(rollbackFor = Exception.class)
    public Note retryIndex(Long userId, Long noteId) {
        Note note = requireNote(userId, noteId, PermissionAction.EDIT);
        if (!NodeType.NOTE.name().equals(note.getNodeType()) || StrUtil.isBlank(note.getContent())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "空笔记不能重建索引");
        }
        note.setParseStatus("PENDING");
        noteRepository.update(note, note.getUserId());
        if (!noteContentIndexPublisher.publishUpdated(note)) {
            note.setParseStatus("FAILED");
            noteRepository.update(note, note.getUserId());
        }
        return note;
    }

    public NoteSummaryDTO getActiveNote(Long userId, Long noteId) {
        Note note = requireNote(userId, noteId, PermissionAction.READ);
        return NoteSummaryDTO.builder().noteId(note.getId()).libraryId(note.getLibraryId()).title(note.getTitle())
                .content(note.getContent()).version(note.getVersion()).build();
    }

    public List<Long> listActiveNoteIds(Long userId, Long libraryId) {
        Library library = requireLibrary(userId, libraryId, PermissionAction.READ);
        return noteRepository.findByLibraryId(libraryId, library.getUserId(), false).stream().map(Note::getId).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Note updateNote(Long userId, Long noteId, String title, String content, NoteType noteType) {
        Note note = requireNote(userId, noteId, PermissionAction.EDIT);
        if (NodeType.FOLDER.name().equals(note.getNodeType())) {
            if (content != null || noteType != null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "文件夹不能保存正文或笔记类型");
            }
            if (title != null) {
                if (StrUtil.isBlank(title)) {
                    throw new BusinessException(ResultCode.BAD_REQUEST, "folder title cannot be blank");
                }
                note.setTitle(title.trim());
                noteRepository.update(note, note.getUserId());
            }
            return note;
        }
        String nextTitle = title == null ? note.getTitle() : title.trim();
        if (StrUtil.isBlank(nextTitle)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "note title cannot be blank");
        }
        String nextContent = content == null ? note.getContent() : content;
        boolean changed = !Objects.equals(nextTitle, note.getTitle()) || !Objects.equals(nextContent, note.getContent());
        if (changed) {
            saveSnapshot(note, userId);
            note.setTitle(nextTitle);
            note.setContent(nextContent);
            note.setVersion(note.getVersion() + 1);
            note.setParseStatus("PENDING");
        }
        if (noteType != null) {
            note.setNoteType(noteType.name());
        }
        noteRepository.update(note, note.getUserId());
        if (changed) {
            noteLinkService.refreshLibraryLinks(note.getUserId(), note.getLibraryId());
            publishContentChange(note);
            markCommentAnchorsOrphaned(note.getId());
        }
        return note;
    }

    @Transactional(rollbackFor = Exception.class)
    public Note materializeCollaborativeNote(Long noteId, String title, String content) {
        Note note = noteRepository.findById(noteId);
        if (note == null) throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        NoteType noteType = note.getNoteType() == null ? null : NoteType.valueOf(note.getNoteType());
        return updateNote(note.getUserId(), noteId, title, content, noteType);
    }

    @Transactional(rollbackFor = Exception.class)
    public void move(Long userId, Long noteId, Long targetParentId, Integer sortOrder) {
        Note note = requireNote(userId, noteId, PermissionAction.EDIT);
        List<Note> nodes = noteRepository.findByLibraryId(note.getLibraryId(), note.getUserId(), false);
        Note parent = requireParent(nodes, note.getLibraryId(), targetParentId);
        if (targetParentId != null && targetParentId.equals(noteId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不能移动到自身");
        }
        ensureNoCycle(noteId, parent, nodes);
        int newDepth = depth(parent, nodes) + subtreeHeight(noteId, nodes);
        if (newDepth > MAX_NODE_DEPTH) {
            throw new BusinessException(ResultCode.FOLDER_MAX_DEPTH);
        }
        note.setParentId(targetParentId);
        if (sortOrder != null) {
            note.setSortOrder(sortOrder);
        }
        noteRepository.update(note, note.getUserId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void reorder(Long userId, Long libraryId, Long parentId, List<Long> noteIds) {
        Library library = requireLibrary(userId, libraryId, PermissionAction.EDIT);
        if (noteIds.stream().distinct().count() != noteIds.size()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "排序节点不能重复");
        }
        List<Note> nodes = noteRepository.findByLibraryId(libraryId, library.getUserId(), false);
        for (int index = 0; index < noteIds.size(); index++) {
            Note note = findNode(nodes, noteIds.get(index));
            if (note == null || !Objects.equals(note.getParentId(), parentId)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "排序节点不属于目标目录");
            }
            note.setSortOrder(index);
            noteRepository.update(note, note.getUserId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteNote(Long userId, Long noteId) {
        Note note = requireNote(userId, noteId, PermissionAction.EDIT);
        List<Note> nodes = noteRepository.findByLibraryId(note.getLibraryId(), note.getUserId(), false);
        deleteSubtree(note.getUserId(), noteId, nodes);
        noteLinkService.refreshLibraryLinks(note.getUserId(), note.getLibraryId());
    }

    public List<NoteVersion> listVersions(Long userId, Long noteId) {
        requireNote(userId, noteId, PermissionAction.READ);
        return noteVersionMapper.selectList(new LambdaQueryWrapper<NoteVersion>()
                .eq(NoteVersion::getNoteId, noteId)
                .orderByDesc(NoteVersion::getVersion));
    }

    @Transactional(rollbackFor = Exception.class)
    public Note restoreVersion(Long userId, Long noteId, Integer version) {
        Note note = requireNote(userId, noteId, PermissionAction.EDIT);
        NoteVersion target = noteVersionMapper.selectOne(new LambdaQueryWrapper<NoteVersion>()
                .eq(NoteVersion::getNoteId, noteId).eq(NoteVersion::getVersion, version).last("LIMIT 1"));
        if (target == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "笔记版本不存在");
        }
        saveSnapshot(note, userId);
        note.setTitle(target.getTitle());
        note.setContent(target.getContent());
        note.setVersion(note.getVersion() + 1);
        note.setParseStatus("PENDING");
        noteRepository.update(note, note.getUserId());
        noteLinkService.refreshLibraryLinks(note.getUserId(), note.getLibraryId());
        publishContentChange(note);
        markCommentAnchorsOrphaned(note.getId());
        return note;
    }

    public List<Note> recycleNotes(Long userId) {
        return noteRepository.findDeletedByUserId(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void restoreNote(Long userId, Long noteId) {
        Note deleted = noteRepository.findDeletedByIdAndUserId(noteId, userId);
        if (deleted == null) {
            throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        }
        Map<Long, List<Note>> deletedChildren = new HashMap<>();
        for (Note candidate : noteRepository.findDeletedByUserId(userId)) {
            if (candidate.getParentId() != null && candidate.getLibraryId().equals(deleted.getLibraryId())) {
                deletedChildren.computeIfAbsent(candidate.getParentId(), ignored -> new ArrayList<>()).add(candidate);
            }
        }
        Long parentId = deleted.getParentId();
        Note parent = parentId == null ? null : noteRepository.findByIdAndUserId(parentId, userId);
        Long restoredParentId = parent != null && parent.getLibraryId().equals(deleted.getLibraryId()) ? parentId : null;
        restoreSubtree(userId, deleted, restoredParentId, deletedChildren);
        noteLinkService.refreshLibraryLinks(userId, deleted.getLibraryId());
    }

    private void restoreSubtree(Long userId, Note node, Long parentId, Map<Long, List<Note>> deletedChildren) {
        noteRepository.restore(node.getId(), userId, parentId);
        if (NodeType.NOTE.name().equals(node.getNodeType()) && StrUtil.isNotBlank(node.getContent())) {
            publishContentChange(node);
        }
        for (Note child : deletedChildren.getOrDefault(node.getId(), List.of())) {
            restoreSubtree(userId, child, node.getId(), deletedChildren);
        }
    }

    private void attachChildren(Long parentId, List<NoteTreeNode> target, Map<Long, List<NoteTreeNode>> byParent) {
        List<NoteTreeNode> nodes = new ArrayList<>(byParent.getOrDefault(parentId, List.of()));
        nodes.sort(Comparator.comparing(NoteTreeNode::sortOrder).thenComparing(NoteTreeNode::noteId));
        for (NoteTreeNode node : nodes) {
            List<NoteTreeNode> childTarget = new ArrayList<>();
            attachChildren(node.noteId(), childTarget, byParent);
            target.add(new NoteTreeNode(node.noteId(), node.parentId(), node.nodeType(), node.title(), node.noteType(),
                    node.parseStatus(), node.sortOrder(), childTarget));
        }
    }

    public ResourceDescriptorDTO getResourceDescriptor(ResourceType resourceType, Long resourceId) {
        if (resourceType == ResourceType.LIBRARY) {
            Library library = libraryRepository.findById(resourceId);
            if (library == null) return null;
            return new ResourceDescriptorDTO(library.getId(), ResourceType.LIBRARY, library.getUserId(), library.getId(), null);
        }
        Note note = noteRepository.findById(resourceId);
        if (note == null) return null;
        ResourceType actualType = NodeType.FOLDER.name().equals(note.getNodeType()) ? ResourceType.FOLDER : ResourceType.NOTE;
        return new ResourceDescriptorDTO(note.getId(), actualType, note.getUserId(), note.getLibraryId(), note.getParentId());
    }

    private Library requireLibrary(Long userId, Long libraryId, PermissionAction action) {
        Library ownedLibrary = libraryRepository.findByIdAndUserId(libraryId, userId);
        if (ownedLibrary != null) {
            return ownedLibrary;
        }
        Library library = libraryRepository.findById(libraryId);
        if (library == null || !hasPermission(userId, ResourceType.LIBRARY, libraryId, library.getUserId(), action)) {
            throw new BusinessException(ResultCode.LIBRARY_NOT_FOUND);
        }
        return library;
    }

    private Note requireNote(Long userId, Long noteId, PermissionAction action) {
        Note ownedNote = noteRepository.findByIdAndUserId(noteId, userId);
        if (ownedNote != null) {
            return ownedNote;
        }
        Note note = noteRepository.findById(noteId);
        if (note == null) throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        ResourceType resourceType = NodeType.FOLDER.name().equals(note.getNodeType()) ? ResourceType.FOLDER : ResourceType.NOTE;
        if (!hasPermission(userId, resourceType, noteId, note.getUserId(), action)) {
            throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        }
        return note;
    }

    private boolean hasPermission(Long userId, ResourceType resourceType, Long resourceId, Long ownerUserId,
                                  PermissionAction action) {
        return Objects.equals(userId, ownerUserId)
                || collaborationPermissionFacade != null
                && collaborationPermissionFacade.hasPermission(userId, resourceType, resourceId, action);
    }

    private void markCommentAnchorsOrphaned(Long noteId) {
        if (collaborationPermissionFacade != null) collaborationPermissionFacade.markNoteAnchorsOrphaned(noteId);
    }

    private Note requireParent(List<Note> nodes, Long libraryId, Long parentId) {
        if (parentId == null) {
            return null;
        }
        Note parent = findNode(nodes, parentId);
        if (parent == null || !libraryId.equals(parent.getLibraryId()) || !NodeType.FOLDER.name().equals(parent.getNodeType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "父节点必须是当前知识库下的文件夹");
        }
        return parent;
    }

    private int depth(Note node, List<Note> nodes) {
        int depth = 0;
        for (Note current = node; current != null; current = findNode(nodes, current.getParentId())) {
            depth++;
        }
        return depth;
    }

    private int subtreeHeight(Long noteId, List<Note> nodes) {
        int height = 1;
        for (Note node : nodes) {
            if (noteId.equals(node.getParentId())) {
                height = Math.max(height, subtreeHeight(node.getId(), nodes) + 1);
            }
        }
        return height;
    }

    private void ensureNoCycle(Long noteId, Note targetParent, List<Note> nodes) {
        for (Note current = targetParent; current != null; current = findNode(nodes, current.getParentId())) {
            if (noteId.equals(current.getId())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "不能移动到子节点中");
            }
        }
    }

    private void deleteSubtree(Long userId, Long noteId, List<Note> nodes) {
        for (Note node : nodes) {
            if (noteId.equals(node.getParentId())) {
                deleteSubtree(userId, node.getId(), nodes);
            }
        }
        noteLinkService.deleteLinksForNote(noteId);
        noteFlashcardPublisher.publishNoteDeleted(userId, noteId);
        noteContentIndexPublisher.publishDeleted(userId, noteId);
        noteGraphProjectionPublisher.publishNoteDeleted(userId, noteId);
        noteRepository.delete(noteId, userId);
    }

    private void saveSnapshot(Note note, Long userId) {
        NoteVersion snapshot = new NoteVersion();
        snapshot.setNoteId(note.getId());
        snapshot.setVersion(note.getVersion());
        snapshot.setTitle(note.getTitle());
        snapshot.setContent(note.getContent());
        snapshot.setCreatedBy(userId);
        noteVersionMapper.insert(snapshot);
    }

    private Note findNode(List<Note> nodes, Long noteId) {
        if (noteId == null) {
            return null;
        }
        return nodes.stream().filter(node -> noteId.equals(node.getId())).findFirst().orElse(null);
    }

    private void publishContentChange(Note note) {
        if (StrUtil.isBlank(note.getContent())) {
            note.setParseStatus("NONE");
            noteRepository.update(note, note.getUserId());
            noteContentIndexPublisher.publishDeleted(note.getUserId(), note.getId());
            noteGraphProjectionPublisher.publishNoteDeleted(note.getUserId(), note.getId());
            return;
        }
        note.setParseStatus("PENDING");
        noteRepository.update(note, note.getUserId());
        if (!noteContentIndexPublisher.publishUpdated(note)) {
            note.setParseStatus("FAILED");
            noteRepository.update(note, note.getUserId());
        }
    }
}
