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
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public List<Library> listLibraries(Long userId) {
        return libraryRepository.findByUserId(userId, false);
    }

    public Library getLibrary(Long userId, Long libraryId) {
        return requireLibrary(userId, libraryId);
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
        Library library = requireLibrary(userId, libraryId);
        if (name != null) {
            if (StrUtil.isBlank(name)) throw new BusinessException(ResultCode.BAD_REQUEST, "知识库名称不能为空");
            library.setName(name.trim());
        }
        if (type != null) library.setType(type.name());
        if (description != null) library.setDescription(description);
        if (sortOrder != null) library.setSortOrder(sortOrder);
        libraryRepository.update(library, userId);
        return library;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteLibrary(Long userId, Long libraryId) {
        requireLibrary(userId, libraryId);
        for (Note note : noteRepository.findByLibraryId(libraryId, userId, false)) {
            noteRepository.delete(note.getId(), userId);
        }
        libraryRepository.delete(libraryId, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void restoreLibrary(Long userId, Long libraryId) {
        if (libraryRepository.findDeletedByIdAndUserId(libraryId, userId) == null || !libraryRepository.restore(libraryId, userId)) {
            throw new BusinessException(ResultCode.LIBRARY_NOT_FOUND);
        }
    }

    public List<NoteTreeNode> tree(Long userId, Long libraryId) {
        requireLibrary(userId, libraryId);
        Map<Long, List<NoteTreeNode>> children = new HashMap<>();
        List<NoteTreeNode> roots = new ArrayList<>();
        for (Note note : noteRepository.findByLibraryId(libraryId, userId, false)) {
            NoteTreeNode node = NoteTreeNode.from(note);
            children.computeIfAbsent(note.getParentId(), ignored -> new ArrayList<>()).add(node);
        }
        attachChildren(null, roots, children);
        return roots;
    }

    @Transactional(rollbackFor = Exception.class)
    public Note createNote(Long userId, Long libraryId, Long parentId, NodeType nodeType, String title,
                           NoteType noteType, String content, Integer sortOrder) {
        requireLibrary(userId, libraryId);
        List<Note> nodes = noteRepository.findByLibraryId(libraryId, userId, false);
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
        note.setUserId(userId);
        note.setLibraryId(libraryId);
        note.setParentId(parentId);
        note.setNodeType(nodeType.name());
        note.setTitle(title.trim());
        note.setNoteType(noteType == null ? null : noteType.name());
        note.setContent(nodeType == NodeType.FOLDER ? "" : Objects.requireNonNullElse(content, ""));
        note.setSortOrder(sortOrder == null ? 0 : sortOrder);
        note.setVersion(1);
        note.setParseStatus(nodeType == NodeType.NOTE ? "PENDING" : "NONE");
        noteRepository.save(note);
        return note;
    }

    public Note getNote(Long userId, Long noteId) {
        return requireNote(userId, noteId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Note updateNote(Long userId, Long noteId, String title, String content, NoteType noteType) {
        Note note = requireNote(userId, noteId);
        if (NodeType.FOLDER.name().equals(note.getNodeType())) {
            if (content != null || noteType != null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "文件夹不能保存正文或笔记类型");
            }
            if (title != null) {
                note.setTitle(title.trim());
                noteRepository.update(note, userId);
            }
            return note;
        }
        String nextTitle = title == null ? note.getTitle() : title.trim();
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
        noteRepository.update(note, userId);
        return note;
    }

    @Transactional(rollbackFor = Exception.class)
    public void move(Long userId, Long noteId, Long targetParentId, Integer sortOrder) {
        Note note = requireNote(userId, noteId);
        List<Note> nodes = noteRepository.findByLibraryId(note.getLibraryId(), userId, false);
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
        noteRepository.update(note, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void reorder(Long userId, Long libraryId, Long parentId, List<Long> noteIds) {
        requireLibrary(userId, libraryId);
        if (noteIds.stream().distinct().count() != noteIds.size()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "排序节点不能重复");
        }
        List<Note> nodes = noteRepository.findByLibraryId(libraryId, userId, false);
        for (int index = 0; index < noteIds.size(); index++) {
            Note note = findNode(nodes, noteIds.get(index));
            if (note == null || !Objects.equals(note.getParentId(), parentId)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "排序节点不属于目标目录");
            }
            note.setSortOrder(index);
            noteRepository.update(note, userId);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteNote(Long userId, Long noteId) {
        Note note = requireNote(userId, noteId);
        List<Note> nodes = noteRepository.findByLibraryId(note.getLibraryId(), userId, false);
        deleteSubtree(userId, noteId, nodes);
    }

    public List<NoteVersion> listVersions(Long userId, Long noteId) {
        requireNote(userId, noteId);
        return noteVersionMapper.selectList(new LambdaQueryWrapper<NoteVersion>()
                .eq(NoteVersion::getNoteId, noteId)
                .orderByDesc(NoteVersion::getVersion));
    }

    @Transactional(rollbackFor = Exception.class)
    public Note restoreVersion(Long userId, Long noteId, Integer version) {
        Note note = requireNote(userId, noteId);
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
        noteRepository.update(note, userId);
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
        Long parentId = deleted.getParentId();
        Note parent = parentId == null ? null : noteRepository.findByIdAndUserId(parentId, userId);
        noteRepository.restore(noteId, userId, parent != null && parent.getLibraryId().equals(deleted.getLibraryId()) ? parentId : null);
    }

    private void attachChildren(Long parentId, List<NoteTreeNode> target, Map<Long, List<NoteTreeNode>> byParent) {
        List<NoteTreeNode> nodes = byParent.getOrDefault(parentId, List.of());
        nodes.sort(Comparator.comparing(NoteTreeNode::sortOrder).thenComparing(NoteTreeNode::noteId));
        for (NoteTreeNode node : nodes) {
            List<NoteTreeNode> childTarget = new ArrayList<>();
            attachChildren(node.noteId(), childTarget, byParent);
            target.add(new NoteTreeNode(node.noteId(), node.parentId(), node.nodeType(), node.title(), node.noteType(),
                    node.parseStatus(), node.sortOrder(), childTarget));
        }
    }

    private Library requireLibrary(Long userId, Long libraryId) {
        Library library = libraryRepository.findByIdAndUserId(libraryId, userId);
        if (library == null) {
            throw new BusinessException(ResultCode.LIBRARY_NOT_FOUND);
        }
        return library;
    }

    private Note requireNote(Long userId, Long noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId);
        if (note == null) {
            throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        }
        return note;
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
}
