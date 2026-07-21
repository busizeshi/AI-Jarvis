package com.notegather.biz.knowledge.application.service;

import com.notegather.biz.knowledge.domain.enums.NodeType;
import com.notegather.biz.knowledge.domain.enums.NoteType;
import com.notegather.biz.knowledge.domain.model.Library;
import com.notegather.biz.knowledge.domain.model.Note;
import com.notegather.biz.knowledge.application.dto.NoteTreeNode;
import com.notegather.biz.knowledge.domain.repository.LibraryRepository;
import com.notegather.biz.knowledge.domain.repository.NoteRepository;
import com.notegather.biz.knowledge.infrastructure.persistence.note.mapper.NoteVersionMapper;
import com.notegather.biz.knowledge.infrastructure.messaging.NoteGraphProjectionPublisher;
import com.notegather.biz.knowledge.infrastructure.messaging.NoteFlashcardPublisher;
import com.notegather.biz.knowledge.infrastructure.messaging.NoteContentIndexPublisher;
import com.notegather.common.core.exception.BusinessException;
import com.notegather.common.core.result.ResultCode;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
        KnowledgeService service = service(libraries, notes);

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
        KnowledgeService service = service(libraries, notes);

        BusinessException error = assertThrows(BusinessException.class, () -> service.move(USER_ID, 10L, 11L, 0));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), error.getCode());
    }

    @Test
    void materializedCollaborationContentUsesStandardContentChangePipeline() {
        LibraryRepository libraries = mock(LibraryRepository.class);
        NoteRepository notes = mock(NoteRepository.class);
        Note note = note(10L, "before");
        when(notes.findById(10L)).thenReturn(note);
        NoteContentIndexPublisher indexPublisher = mock(NoteContentIndexPublisher.class);
        KnowledgeService service = new KnowledgeService(libraries, notes, mock(NoteVersionMapper.class), mock(NoteLinkService.class),
                mock(NoteGraphProjectionPublisher.class), mock(NoteFlashcardPublisher.class), indexPublisher);

        Note updated = service.materializeCollaborativeNote(10L, "after", "collaborative content");

        assertEquals("after", updated.getTitle());
        assertEquals("collaborative content", updated.getContent());
        assertEquals(2, updated.getVersion());
        verify(indexPublisher).publishUpdated(updated);
    }

    @Test
    void buildsTreeWhenLeafHasNoChildren() {
        LibraryRepository libraries = mock(LibraryRepository.class);
        NoteRepository notes = mock(NoteRepository.class);
        when(libraries.findByIdAndUserId(LIBRARY_ID, USER_ID)).thenReturn(library());
        Note root = folder(10L, null);
        Note leaf = note(11L, "");
        leaf.setParentId(root.getId());
        when(notes.findByLibraryId(LIBRARY_ID, USER_ID, false)).thenReturn(List.of(root, leaf));

        List<NoteTreeNode> tree = service(libraries, notes).tree(USER_ID, LIBRARY_ID);

        assertEquals(1, tree.size());
        assertEquals(1, tree.getFirst().children().size());
        assertEquals(leaf.getId(), tree.getFirst().children().getFirst().noteId());
    }

    @Test
    void folderRenameDoesNotRequireOrOverwriteNoteContent() {
        LibraryRepository libraries = mock(LibraryRepository.class);
        NoteRepository notes = mock(NoteRepository.class);
        Note folder = folder(10L, null);
        folder.setTitle("Old folder");
        when(notes.findByIdAndUserId(folder.getId(), USER_ID)).thenReturn(folder);

        Note updated = service(libraries, notes).updateNote(USER_ID, folder.getId(), "New folder", null, null);

        assertEquals("New folder", updated.getTitle());
        verify(notes).update(folder, USER_ID);
    }

    @Test
    void restoringFolderRestoresDeletedDescendants() {
        LibraryRepository libraries = mock(LibraryRepository.class);
        NoteRepository notes = mock(NoteRepository.class);
        Note folder = folder(10L, null);
        Note child = note(11L, "");
        child.setParentId(folder.getId());
        when(notes.findDeletedByIdAndUserId(folder.getId(), USER_ID)).thenReturn(folder);
        when(notes.findDeletedByUserId(USER_ID)).thenReturn(List.of(folder, child));
        when(notes.restore(folder.getId(), USER_ID, null)).thenReturn(true);
        when(notes.restore(child.getId(), USER_ID, folder.getId())).thenReturn(true);

        service(libraries, notes).restoreNote(USER_ID, folder.getId());

        verify(notes).restore(folder.getId(), USER_ID, null);
        verify(notes).restore(child.getId(), USER_ID, folder.getId());
    }

    @Test
    void retryIndexMarksNoteFailedWhenPublicationFails() {
        LibraryRepository libraries = mock(LibraryRepository.class);
        NoteRepository notes = mock(NoteRepository.class);
        Note source = note(10L, "content");
        when(notes.findByIdAndUserId(source.getId(), USER_ID)).thenReturn(source);
        NoteContentIndexPublisher publisher = mock(NoteContentIndexPublisher.class);
        KnowledgeService service = new KnowledgeService(libraries, notes, mock(NoteVersionMapper.class),
                mock(NoteLinkService.class), mock(NoteGraphProjectionPublisher.class),
                mock(NoteFlashcardPublisher.class), publisher);

        Note result = service.retryIndex(USER_ID, source.getId());

        assertEquals("FAILED", result.getParseStatus());
        verify(publisher).publishUpdated(source);
    }

    @Test
    void retryIndexRemainsPendingWhenPublicationSucceeds() {
        LibraryRepository libraries = mock(LibraryRepository.class);
        NoteRepository notes = mock(NoteRepository.class);
        Note source = note(10L, "content");
        when(notes.findByIdAndUserId(source.getId(), USER_ID)).thenReturn(source);
        NoteContentIndexPublisher publisher = mock(NoteContentIndexPublisher.class);
        when(publisher.publishUpdated(source)).thenReturn(true);
        KnowledgeService service = new KnowledgeService(libraries, notes, mock(NoteVersionMapper.class),
                mock(NoteLinkService.class), mock(NoteGraphProjectionPublisher.class),
                mock(NoteFlashcardPublisher.class), publisher);

        Note result = service.retryIndex(USER_ID, source.getId());

        assertEquals("PENDING", result.getParseStatus());
    }

    @Test
    void rejectsLibraryDeletionWhenConfirmationNameDoesNotMatch() {
        LibraryRepository libraries = mock(LibraryRepository.class);
        NoteRepository notes = mock(NoteRepository.class);
        Library library = library();
        library.setName("研究资料");
        when(libraries.findByIdAndUserId(LIBRARY_ID, USER_ID)).thenReturn(library);
        KnowledgeService service = service(libraries, notes);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.deleteLibrary(USER_ID, LIBRARY_ID, "研究笔记"));

        assertEquals(ResultCode.BAD_REQUEST.getCode(), error.getCode());
        verify(libraries, never()).delete(LIBRARY_ID, USER_ID);
    }

    @Test
    void deletesLibraryWhenOwnerConfirmsItsExactName() {
        LibraryRepository libraries = mock(LibraryRepository.class);
        NoteRepository notes = mock(NoteRepository.class);
        Library library = library();
        library.setName("研究资料");
        when(libraries.findByIdAndUserId(LIBRARY_ID, USER_ID)).thenReturn(library);
        when(notes.findByLibraryId(LIBRARY_ID, USER_ID, false)).thenReturn(List.of());

        service(libraries, notes).deleteLibrary(USER_ID, LIBRARY_ID, "研究资料");

        verify(libraries).delete(LIBRARY_ID, USER_ID);
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
        note.setSortOrder(0);
        return note;
    }

    private Note note(Long id, String content) {
        Note note = new Note();
        note.setId(id);
        note.setUserId(USER_ID);
        note.setLibraryId(LIBRARY_ID);
        note.setNodeType(NodeType.NOTE.name());
        note.setNoteType(NoteType.NOTE.name());
        note.setTitle("before");
        note.setContent(content);
        note.setSortOrder(0);
        note.setVersion(1);
        return note;
    }

    private KnowledgeService service(LibraryRepository libraries, NoteRepository notes) {
        return new KnowledgeService(libraries, notes, mock(NoteVersionMapper.class), mock(NoteLinkService.class),
                mock(NoteGraphProjectionPublisher.class), mock(NoteFlashcardPublisher.class),
                mock(NoteContentIndexPublisher.class));
    }
}
