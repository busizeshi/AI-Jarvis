package com.notegather.biz.knowledge.application.service;

import cn.hutool.core.util.StrUtil;
import com.notegather.biz.knowledge.domain.model.Library;
import com.notegather.biz.knowledge.domain.model.Note;
import com.notegather.biz.knowledge.domain.repository.LibraryRepository;
import com.notegather.biz.knowledge.domain.repository.NoteRepository;
import com.notegather.common.api.knowledge.dto.FileNoteCreateRequest;
import com.notegather.common.api.knowledge.dto.FileNoteDTO;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileNoteServiceImpl implements FileNoteService {

    private static final String NODE_TYPE_FOLDER = "FOLDER";
    private static final String NODE_TYPE_NOTE = "NOTE";
    private static final String NOTE_TYPE_NOTE = "NOTE";
    private static final String PARSE_STATUS_PENDING = "PENDING";

    private final LibraryRepository libraryRepository;
    private final NoteRepository noteRepository;
    private final NoteLinkService noteLinkService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileNoteDTO createFileNote(FileNoteCreateRequest request) {
        requireLibrary(request.getUserId(), request.getLibraryId());
        requireParentFolder(request.getUserId(), request.getLibraryId(), request.getParentId());
        String title = fileTitle(request.getFileName());
        Note note = new Note();
        note.setUserId(request.getUserId());
        note.setLibraryId(request.getLibraryId());
        note.setParentId(request.getParentId());
        note.setNodeType(NODE_TYPE_NOTE);
        note.setNoteType(NOTE_TYPE_NOTE);
        note.setTitle(title);
        note.setContent("");
        note.setSortOrder(0);
        note.setVersion(1);
        note.setParseStatus(PARSE_STATUS_PENDING);
        noteRepository.save(note);
        return FileNoteDTO.builder().noteId(note.getId()).noteTitle(title).build();
    }

    @Override
    public String getNoteTitle(Long userId, Long noteId) {
        Note note = requireNote(userId, noteId);
        return note.getTitle();
    }

    @Override
    public void updateParseStatus(Long userId, Long noteId, String parseStatus) {
        if (noteId == null) {
            return;
        }
        noteRepository.updateParseStatus(noteId, userId, parseStatus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyParsedContent(Long userId, Long noteId, Integer expectedVersion, String content) {
        Note note = requireNote(userId, noteId);
        if (!note.getVersion().equals(expectedVersion) || StrUtil.isNotBlank(note.getContent())) {
            return false;
        }
        note.setContent(content == null ? "" : content);
        note.setParseStatus("DONE");
        noteRepository.update(note, userId);
        noteLinkService.refreshLibraryLinks(userId, note.getLibraryId());
        return true;
    }

    private void requireLibrary(Long userId, Long libraryId) {
        Library library = libraryRepository.findByIdAndUserId(libraryId, userId);
        if (library == null) {
            throw new BusinessException(ResultCode.LIBRARY_NOT_FOUND);
        }
    }

    private void requireParentFolder(Long userId, Long libraryId, Long parentId) {
        if (parentId == null) {
            return;
        }
        Note parent = requireNote(userId, parentId);
        if (!libraryId.equals(parent.getLibraryId()) || !NODE_TYPE_FOLDER.equals(parent.getNodeType())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "父节点必须是当前知识库下的文件夹");
        }
    }

    private Note requireNote(Long userId, Long noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId);
        if (note == null) {
            throw new BusinessException(ResultCode.NOTE_NOT_FOUND);
        }
        return note;
    }

    private String fileTitle(String fileName) {
        String normalized = StrUtil.trim(fileName);
        int extensionIndex = normalized.lastIndexOf('.');
        return extensionIndex > 0 ? normalized.substring(0, extensionIndex) : normalized;
    }
}
