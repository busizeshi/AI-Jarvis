package com.notegather.biz.domain.asset.repository;

import com.notegather.biz.domain.asset.entity.AsyncTask;
import com.notegather.biz.domain.asset.valueobject.TaskId;
import com.notegather.biz.domain.asset.valueobject.TaskStatus;

import java.util.List;
import java.util.Optional;

/**
 * 异步任务仓储接口
 */
public interface AsyncTaskRepository {
    
    /**
     * 保存任务
     */
    AsyncTask save(AsyncTask task);
    
    /**
     * 根据ID查询任务
     */
    Optional<AsyncTask> findById(TaskId taskId);
    
    /**
     * 根据文档ID查询最新任务
     */
    Optional<AsyncTask> findLatestByDocumentId(Long documentId);
    
    /**
     * 查询指定状态的任务列表
     */
    List<AsyncTask> findByStatus(TaskStatus status, int limit);
    
    /**
     * 查询用户的任务列表
     */
    List<AsyncTask> findByUserId(Long userId, int page, int size);
}
