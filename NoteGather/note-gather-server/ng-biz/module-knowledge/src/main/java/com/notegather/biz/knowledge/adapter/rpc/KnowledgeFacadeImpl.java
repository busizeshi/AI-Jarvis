package com.notegather.biz.knowledge.adapter.rpc;

import com.notegather.biz.knowledge.application.service.FileNoteService;
import com.notegather.biz.knowledge.application.service.KnowledgeService;
import com.notegather.biz.knowledge.domain.enums.NodeType;
import com.notegather.biz.knowledge.domain.enums.NoteType;
import com.notegather.biz.knowledge.domain.model.Note;
import com.notegather.common.api.knowledge.KnowledgeFacade;
import com.notegather.common.api.knowledge.dto.FileNoteCreateRequest;
import com.notegather.common.api.knowledge.dto.FileNoteDTO;
import com.notegather.common.api.knowledge.dto.NoteSummaryDTO;
import com.notegather.common.api.collaboration.ResourceType;
import com.notegather.common.api.collaboration.dto.ResourceDescriptorDTO;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Primary
@DubboService
@RequiredArgsConstructor
public class KnowledgeFacadeImpl implements KnowledgeFacade {

    private final FileNoteService fileNoteService;
    private final KnowledgeService knowledgeService;

    @Override
    public FileNoteDTO createFileNote(FileNoteCreateRequest request) {
        return fileNoteService.createFileNote(request);
    }

    @Override
    public String getNoteTitle(Long userId, Long noteId) {
        return fileNoteService.getNoteTitle(userId, noteId);
    }

    @Override
    public void updateParseStatus(Long userId, Long noteId, String parseStatus) {
        fileNoteService.updateParseStatus(userId, noteId, parseStatus);
    }

    @Override
    public boolean applyParsedContent(Long userId, Long noteId, Integer expectedVersion, String content) {
        return fileNoteService.applyParsedContent(userId, noteId, expectedVersion, content);
    }

    @Override
    public NoteSummaryDTO getActiveNote(Long userId, Long noteId) {
        return knowledgeService.getActiveNote(userId, noteId);
    }

    @Override
    public NoteSummaryDTO createWorkflowDraft(Long userId, Long libraryId, String title, String content) {
        Note note = knowledgeService.createNote(userId, libraryId, null, NodeType.NOTE, title, NoteType.SCRATCH, content, 0);
        return new NoteSummaryDTO(note.getId(), note.getLibraryId(), note.getTitle(), note.getContent(), note.getVersion());
    }

    @Override
    public List<Long> listActiveNoteIds(Long userId, Long libraryId) {
        return knowledgeService.listActiveNoteIds(userId, libraryId);
    }

    @Override
    public ResourceDescriptorDTO getResourceDescriptor(ResourceType resourceType, Long resourceId) {
        return knowledgeService.getResourceDescriptor(resourceType, resourceId);
    }
}
