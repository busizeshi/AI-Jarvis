package com.notegather.biz.file.domain.repository;

import com.notegather.biz.file.domain.model.ParseTask;

public interface ParseTaskRepository {

    void save(ParseTask task);

    ParseTask findById(Long taskId);

    ParseTask findActiveByFileId(Long fileId);

    ParseTask findLatestByFileId(Long fileId);

    void update(ParseTask task);
}
