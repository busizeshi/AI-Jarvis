package com.notegather.biz.knowledge.application.service;

import com.notegather.common.api.knowledge.dto.FileNoteCreateRequest;
import com.notegather.common.api.knowledge.dto.FileNoteDTO;

public interface FileNoteService {

    FileNoteDTO createFileNote(FileNoteCreateRequest request);

    String getNoteTitle(Long userId, Long noteId);

    void updateParseStatus(Long userId, Long noteId, String parseStatus);

    boolean applyParsedContent(Long userId, Long noteId, Integer expectedVersion, String content);
}
