package com.notegather.biz.knowledge.application.service;

import com.notegather.biz.knowledge.domain.enums.NodeType;
import com.notegather.biz.knowledge.domain.enums.NoteType;
import com.notegather.biz.knowledge.domain.model.Library;
import com.notegather.biz.knowledge.domain.model.Note;
import com.notegather.biz.knowledge.domain.repository.LibraryRepository;
import com.notegather.biz.knowledge.domain.repository.NoteRepository;
import com.notegather.biz.knowledge.infrastructure.persistence.note.mapper.NoteVersionMapper;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KnowledgeServiceTest {

    private static final long USER_ID = 1L;
    private static final long LIBRARY_ID = 2L;

    @Test
    void rejectsCreateBeyondFiveLevels() {
        LibraryRepository libraries = mock(LibraryRepository.class);
        NoteRepository notes = mock(NoteRepository.class);
        when(libraries.findByIdAndUserId(LIBRARY_ID, USER_ID)).thenReturn(library());
        List<Note> nodes = List.of(folder(10L, null), folder(11L, 10L), folder(12L, 11L), folder(13L, 12L), folder(14L, 13L));
        when(notes.findByLibraryId(LIBRARY_ID, USER_ID, false)).thenReturn(nodes);
        KnowledgeService service = new KnowledgeService(libraries, notes, mock(NoteVersionMapper.class));

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.createNote(USER_ID, LIBRARY_ID, 14L, NodeType.NOTE, "too deep", NoteType.NOTE, "", 0));

        assertEquals(ResultCode.FOLDER_MAX_DEPTH.getCode(), error.getCode());
    }

    @Test
    void rejectsMoveIntoDescendant() {
        LibraryRepository libraries = mock(LibraryRepository.class);
        NoteRepository notes = mock(NoteRepository.class);
        Note root = folder(10L, null);
        Note child = folder(11L, 10L);
        when(notes.findByIdAndUserId(10L, USER_ID)).thenReturn(root);
        when(notes.findByLibraryId(LIBRARY_ID, USER_ID, false)).thenReturn(List.of(root, child));
        KnowledgeService service = new KnowledgeService(libraries, notes, mock(NoteVersionMapper.class));

        BusinessException error = assertThrows(BusinessException.class, () -> service.move(USER_ID, 10L, 11L, 0));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), error.getCode());
    }

    private Library library() {
        Library library = new Library();
        library.setId(LIBRARY_ID);
        library.setUserId(USER_ID);
        return library;
    }

    private Note folder(Long id, Long parentId) {
        Note note = new Note();
        note.setId(id);
        note.setUserId(USER_ID);
        note.setLibraryId(LIBRARY_ID);
        note.setParentId(parentId);
        note.setNodeType(NodeType.FOLDER.name());
        return note;
    }
}
