package com.notegather.biz.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 异步任务数据库实体
 */
@Data
@TableName("async_task")
public class AsyncTaskEntity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String taskId;
    
    private String taskType;
    
    private String resourceType;
    
    private Long resourceId;
    
    private Long userId;
    
    private Integer status;
    
    private Integer progress;
    
    private String errorMessage;
    
    private Integer retryCount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
