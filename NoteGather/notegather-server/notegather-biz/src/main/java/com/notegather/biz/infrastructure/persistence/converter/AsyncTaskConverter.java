package com.notegather.biz.infrastructure.persistence.converter;

import com.notegather.biz.domain.asset.entity.AsyncTask;
import com.notegather.biz.domain.asset.valueobject.TaskId;
import com.notegather.biz.domain.asset.valueobject.TaskStatus;
import com.notegather.biz.domain.asset.valueobject.TaskType;
import com.notegather.biz.domain.knowledge.valueobject.DocumentId;
import com.notegather.biz.infrastructure.persistence.entity.AsyncTaskEntity;
import org.springframework.stereotype.Component;

/**
 * AsyncTask 转换器
 */
@Component
public class AsyncTaskConverter {
    
    /**
     * 领域模型转数据库实体
     */
    public AsyncTaskEntity toEntity(AsyncTask task) {
        AsyncTaskEntity entity = new AsyncTaskEntity();
        
        if (task.getId() != null && task.getId().getValue() != null) {
            entity.setId(task.getId().getValue());
        }
        
        entity.setTaskId(generateTaskId(task));
        entity.setTaskType(task.getTaskType().name());
        entity.setResourceType("DOCUMENT");
        entity.setResourceId(task.getRelatedDocumentId().getValue());
        entity.setStatus(task.getStatus().getCode());
        entity.setRetryCount(task.getRetryCount());
        entity.setErrorMessage(task.getErrorMessage());
        entity.setCreatedAt(task.getCreatedAt());
        
        return entity;
    }
    
    /**
     * 数据库实体转领域模型
     */
    public AsyncTask toDomain(AsyncTaskEntity entity) {
        // 使用反射或构造器创建领域对象
        // 这里简化处理，实际应该使用工厂方法
        throw new UnsupportedOperationException("待实现");
    }
    
    /**
     * 生成任务ID（格式：TASK_{timestamp}_{random}）
     */
    private String generateTaskId(AsyncTask task) {
        if (task.getId() != null && task.getId().getValue() != null) {
            return "TASK_" + task.getId().getValue();
        }
        return "TASK_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }
}
