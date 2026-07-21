package com.notegather.common.api.knowledge;

import com.notegather.common.api.knowledge.dto.FileNoteCreateRequest;
import com.notegather.common.api.knowledge.dto.FileNoteDTO;
import com.notegather.common.api.knowledge.dto.NoteSummaryDTO;
import com.notegather.common.api.collaboration.ResourceType;
import com.notegather.common.api.collaboration.dto.ResourceDescriptorDTO;

import java.util.List;

/**
 * 文件模块访问知识库模块的窄接口。
 */
public interface KnowledgeFacade {

    FileNoteDTO createFileNote(FileNoteCreateRequest request);

    String getNoteTitle(Long userId, Long noteId);

    void updateParseStatus(Long userId, Long noteId, String parseStatus);

    boolean applyParsedContent(Long userId, Long noteId, Integer expectedVersion, String content);

    NoteSummaryDTO getActiveNote(Long userId, Long noteId);

    NoteSummaryDTO createWorkflowDraft(Long userId, Long libraryId, String title, String content);

    List<Long> listActiveNoteIds(Long userId, Long libraryId);

    ResourceDescriptorDTO getResourceDescriptor(ResourceType resourceType, Long resourceId);
}
