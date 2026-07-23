package com.notegather.biz.domain.knowledge.aggregate;

import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.valueobject.DocumentId;
import com.notegather.biz.domain.knowledge.valueobject.DocumentStatus;
import com.notegather.biz.domain.knowledge.valueobject.DocumentType;
import com.notegather.biz.domain.knowledge.valueobject.KnowledgeBaseId;

import java.time.LocalDateTime;

/**
 * 文档聚合根
 */
public class Document {
    
    private DocumentId id;
    private KnowledgeBaseId knowledgeBaseId;
    private DocumentId parentId;
    private UserId ownerId;
    private String title;
    private DocumentType type;
    private String content;
    private String contentType;
    private String fileName;
    private Long fileSize;
    private String fileUrl;
    private Integer depth;
    private Integer orderNum;
    private DocumentStatus status;
    private Long currentVersionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;
    
    private Document() {
    }
    
    /**
     * 创建文件夹
     */
    public static Document createFolder(
            KnowledgeBaseId knowledgeBaseId,
            UserId ownerId,
            DocumentId parentId,
            String title,
            Integer depth) {
        
        validateTitle(title);
        validateDepth(depth);
        
        Document doc = new Document();
        doc.knowledgeBaseId = knowledgeBaseId;
        doc.ownerId = ownerId;
        doc.parentId = parentId;
        doc.title = title.trim();
        doc.type = DocumentType.FOLDER;
        doc.contentType = "folder";  // 文件夹的 contentType
        doc.depth = depth;
        doc.orderNum = 0;
        doc.status = DocumentStatus.PUBLISHED;
        doc.deleted = false;
        doc.createdAt = LocalDateTime.now();
        doc.updatedAt = LocalDateTime.now();
        
        return doc;
    }
    
    /**
     * 创建文档
     */
    public static Document createDocument(
            KnowledgeBaseId knowledgeBaseId,
            UserId ownerId,
            DocumentId parentId,
            String title,
            String content,
            Integer depth) {
        
        validateTitle(title);
        validateDepth(depth);
        
        Document doc = new Document();
        doc.knowledgeBaseId = knowledgeBaseId;
        doc.ownerId = ownerId;
        doc.parentId = parentId;
        doc.title = title.trim();
        doc.type = DocumentType.DOCUMENT;
        doc.content = content;
        doc.contentType = "text/markdown";
        doc.depth = depth;
        doc.orderNum = 0;
        doc.status = DocumentStatus.DRAFT;
        doc.deleted = false;
        doc.createdAt = LocalDateTime.now();
        doc.updatedAt = LocalDateTime.now();
        
        return doc;
    }
    
    /**
     * 更新文档内容
     */
    public void updateContent(String title, String content) {
        if (this.type == DocumentType.FOLDER) {
            throw new IllegalStateException("文件夹不能更新内容");
        }
        
        if (title != null && !title.trim().isEmpty()) {
            validateTitle(title);
            this.title = title.trim();
        }
        
        if (content != null) {
            this.content = content;
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新标题
     */
    public void updateTitle(String title) {
        validateTitle(title);
        this.title = title.trim();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 移动文档到新父节点
     */
    public void moveTo(DocumentId newParentId, Integer newDepth) {
        validateDepth(newDepth);
        this.parentId = newParentId;
        this.depth = newDepth;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设置文件信息
     */
    public void setFileInfo(String fileName, Long fileSize, String fileUrl) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileUrl = fileUrl;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 发布文档
     */
    public void publish() {
        this.status = DocumentStatus.PUBLISHED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 设置为草稿
     */
    public void draft() {
        this.status = DocumentStatus.DRAFT;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 软删除
     */
    public void delete() {
        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查是否为所有者
     */
    public boolean isOwnedBy(UserId userId) {
        return this.ownerId.equals(userId);
    }
    
    /**
     * 检查是否为文件夹
     */
    public boolean isFolder() {
        return this.type == DocumentType.FOLDER;
    }
    
    /**
     * 校验标题
     */
    private static void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("文档标题不能为空");
        }
        if (title.length() > 256) {
            throw new IllegalArgumentException("文档标题不能超过256个字符");
        }
    }
    
    /**
     * 校验深度
     */
    private static void validateDepth(Integer depth) {
        if (depth == null || depth < 0 || depth > 4) {
            throw new IllegalArgumentException("文档层级深度必须在0-4之间");
        }
    }
    
    // Getters
    public DocumentId getId() {
        return id;
    }
    
    public KnowledgeBaseId getKnowledgeBaseId() {
        return knowledgeBaseId;
    }
    
    public DocumentId getParentId() {
        return parentId;
    }
    
    public UserId getOwnerId() {
        return ownerId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public DocumentType getType() {
        return type;
    }
    
    public String getContent() {
        return content;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public Integer getDepth() {
        return depth;
    }
    
    public Integer getOrderNum() {
        return orderNum;
    }
    
    public DocumentStatus getStatus() {
        return status;
    }
    
    public Long getCurrentVersionId() {
        return currentVersionId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public boolean isDeleted() {
        return deleted;
    }
    
    // Public setters for repository reconstruction
    public void setId(DocumentId id) {
        this.id = id;
    }
    
    public void setKnowledgeBaseId(KnowledgeBaseId knowledgeBaseId) {
        this.knowledgeBaseId = knowledgeBaseId;
    }
    
    public void setParentId(DocumentId parentId) {
        this.parentId = parentId;
    }
    
    public void setOwnerId(UserId ownerId) {
        this.ownerId = ownerId;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setType(DocumentType type) {
        this.type = type;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    
    public void setDepth(Integer depth) {
        this.depth = depth;
    }
    
    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
    
    public void setStatus(DocumentStatus status) {
        this.status = status;
    }
    
    public void setCurrentVersionId(Long currentVersionId) {
        this.currentVersionId = currentVersionId;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
