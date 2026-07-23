package com.notegather.biz.domain.asset.entity;

import com.notegather.biz.domain.asset.valueobject.TaskId;
import com.notegather.biz.domain.asset.valueobject.TaskStatus;
import com.notegather.biz.domain.asset.valueobject.TaskType;
import com.notegather.biz.domain.knowledge.valueobject.DocumentId;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 异步任务实体
 * 
 * 职责：
 * 1. 管理任务状态机（PENDING → PROCESSING → COMPLETED/FAILED）
 * 2. 记录任务执行进度和结果
 * 3. 支持任务重试
 */
@Getter
public class AsyncTask {
    
    private TaskId id;
    private TaskType taskType;
    private DocumentId relatedDocumentId;
    private TaskStatus status;
    
    private String inputData;       // 输入数据（JSON格式）
    private String outputData;      // 输出数据（JSON格式）
    private String errorMessage;    // 错误信息
    
    private Integer retryCount;     // 重试次数
    private Integer maxRetries;     // 最大重试次数
    
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    
    /**
     * 创建新任务
     */
    public static AsyncTask create(
            TaskType taskType,
            DocumentId documentId,
            String inputData,
            Integer maxRetries) {
        
        AsyncTask task = new AsyncTask();
        task.id = TaskId.create();
        task.taskType = taskType;
        task.relatedDocumentId = documentId;
        task.status = TaskStatus.PENDING;
        task.inputData = inputData;
        task.retryCount = 0;
        task.maxRetries = maxRetries != null ? maxRetries : 3;
        task.createdAt = LocalDateTime.now();
        
        return task;
    }
    
    /**
     * 开始处理任务
     */
    public void start() {
        if (status != TaskStatus.PENDING && status != TaskStatus.FAILED) {
            throw new IllegalStateException("只有PENDING或FAILED状态的任务才能开始执行");
        }
        
        this.status = TaskStatus.PROCESSING;
        this.startedAt = LocalDateTime.now();
        this.retryCount++;
    }
    
    /**
     * 标记任务完成
     */
    public void complete(String outputData) {
        if (status != TaskStatus.PROCESSING) {
            throw new IllegalStateException("只有PROCESSING状态的任务才能标记为完成");
        }
        
        this.status = TaskStatus.COMPLETED;
        this.outputData = outputData;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * 标记任务失败
     */
    public void fail(String errorMessage) {
        if (status != TaskStatus.PROCESSING) {
            throw new IllegalStateException("只有PROCESSING状态的任务才能标记为失败");
        }
        
        this.status = TaskStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }
    
    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return status == TaskStatus.FAILED && retryCount < maxRetries;
    }
    
    /**
     * 是否为文档解析任务
     */
    public boolean isDocumentParseTask() {
        return taskType == TaskType.DOCUMENT_PARSE;
    }
    
    /**
     * 获取任务执行时长（毫秒）
     */
    public Long getExecutionDuration() {
        if (startedAt == null || completedAt == null) {
            return null;
        }
        return java.time.Duration.between(startedAt, completedAt).toMillis();
    }
}
