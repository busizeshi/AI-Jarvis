package com.notegather.biz.interfaces.rest.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异步任务 DTO
 */
@Data
public class AsyncTaskDTO {
    
    private Long id;
    
    private String taskId;
    
    private String taskType;
    
    private Integer status;
    
    private String statusDesc;
    
    private Long documentId;
    
    private String errorMessage;
    
    private Integer retryCount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;
    
    private Long executionDuration;  // 执行时长（毫秒）
}
