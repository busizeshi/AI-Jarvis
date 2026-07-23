package com.notegather.biz.infrastructure.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 生产者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentEventProducer {
    
    private final RocketMQTemplate rocketMQTemplate;
    
    /**
     * Topic 定义
     */
    public static final String TOPIC_DOCUMENT_PARSE = "ng.document.parse";
    
    /**
     * 发送文档解析请求事件
     */
    public void sendDocumentParseRequest(Long documentId, String fileUrl, String taskId) {
        DocumentParseEvent event = new DocumentParseEvent();
        event.setDocumentId(documentId);
        event.setFileUrl(fileUrl);
        event.setTaskId(taskId);
        event.setTimestamp(System.currentTimeMillis());
        
        try {
            rocketMQTemplate.convertAndSend(TOPIC_DOCUMENT_PARSE, event);
            log.info("发送文档解析事件成功: documentId={}, taskId={}", documentId, taskId);
        } catch (Exception e) {
            log.error("发送文档解析事件失败: documentId={}, taskId={}", documentId, taskId, e);
            throw new RuntimeException("发送解析事件失败", e);
        }
    }
    
    /**
     * 文档解析事件
     */
    public static class DocumentParseEvent {
        private Long documentId;
        private String fileUrl;
        private String taskId;
        private Long timestamp;
        
        public Long getDocumentId() {
            return documentId;
        }
        
        public void setDocumentId(Long documentId) {
            this.documentId = documentId;
        }
        
        public String getFileUrl() {
            return fileUrl;
        }
        
        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }
        
        public String getTaskId() {
            return taskId;
        }
        
        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }
        
        public Long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
