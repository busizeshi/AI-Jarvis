package com.notegather.common.api.knowledge;

import com.notegather.common.api.knowledge.dto.FileNoteCreateRequest;
import com.notegather.common.api.knowledge.dto.FileNoteDTO;

/**
 * 文件模块访问知识库模块的窄接口。
 */
public interface KnowledgeFacade {

    FileNoteDTO createFileNote(FileNoteCreateRequest request);

    String getNoteTitle(Long userId, Long noteId);

    void updateParseStatus(Long userId, Long noteId, String parseStatus);
}
