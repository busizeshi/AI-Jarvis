package com.notegather.biz.application.service;

import cn.dev33.satoken.stp.StpUtil;
import com.notegather.biz.domain.asset.entity.AsyncTask;
import com.notegather.biz.domain.asset.repository.AsyncTaskRepository;
import com.notegather.biz.domain.asset.valueobject.TaskId;
import com.notegather.biz.domain.asset.valueobject.TaskType;
import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.aggregate.Document;
import com.notegather.biz.domain.knowledge.repository.DocumentRepository;
import com.notegather.biz.domain.knowledge.valueobject.DocumentId;
import com.notegather.biz.infrastructure.mq.DocumentEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 异步任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTaskService {
    
    private final DocumentRepository documentRepository;
    private final AsyncTaskRepository asyncTaskRepository;
    private final DocumentEventProducer documentEventProducer;
    
    /**
     * 创建文档解析任务并发送事件
     */
    @Transactional(rollbackFor = Exception.class)
    public AsyncTask createDocumentParseTask(Long documentId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        UserId userId = UserId.of(currentUserId);
        
        // 1. 校验文档权限
        Document document = documentRepository.findById(DocumentId.of(documentId))
                .orElseThrow(() -> new RuntimeException("文档不存在"));
        
        if (!document.isOwnedBy(userId)) {
            throw new RuntimeException("无权限操作该文档");
        }
        
        if (document.getFileUrl() == null || document.getFileUrl().isEmpty()) {
            throw new RuntimeException("文档未上传文件，无法解析");
        }
        
        // 2. 创建异步任务
        String inputData = String.format(
            "{\"documentId\":%d,\"fileUrl\":\"%s\"}",
            documentId,
            document.getFileUrl()
        );
        
        AsyncTask task = AsyncTask.create(
            TaskType.DOCUMENT_PARSE,
            DocumentId.of(documentId),
            inputData,
            3  // 最大重试3次
        );
        
        // 3. 保存任务
        asyncTaskRepository.save(task);
        
        // 4. 发送解析事件到 RocketMQ
        String taskId = "TASK_" + task.getId().getValue();
        documentEventProducer.sendDocumentParseRequest(
            documentId,
            document.getFileUrl(),
            taskId
        );
        
        log.info("创建文档解析任务成功: documentId={}, taskId={}", documentId, taskId);
        
        return task;
    }
    
    /**
     * 查询任务详情
     */
    public AsyncTask getTask(Long taskId) {
        return asyncTaskRepository.findById(TaskId.of(taskId))
                .orElseThrow(() -> new RuntimeException("任务不存在"));
    }
}
