package com.notegather.biz.knowledge.adapter.rpc;

import com.notegather.biz.knowledge.application.service.FileNoteService;
import com.notegather.common.api.knowledge.KnowledgeFacade;
import com.notegather.common.api.knowledge.dto.FileNoteCreateRequest;
import com.notegather.common.api.knowledge.dto.FileNoteDTO;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@RequiredArgsConstructor
public class KnowledgeFacadeImpl implements KnowledgeFacade {

    private final FileNoteService fileNoteService;

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
}
