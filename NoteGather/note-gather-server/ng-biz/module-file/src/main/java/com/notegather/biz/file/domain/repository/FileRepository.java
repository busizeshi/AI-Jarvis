package com.notegather.biz.file.domain.repository;

import com.notegather.biz.file.domain.model.FileRecord;

public interface FileRepository {

    void save(FileRecord file);

    FileRecord findById(Long fileId);

    FileRecord findByIdAndUserId(Long fileId, Long userId);

    void bindNote(Long fileId, Long userId, Long noteId);

    void updateParseResult(Long fileId, Long userId, String parseStatus, Integer chunkCount, String parseError);
}
