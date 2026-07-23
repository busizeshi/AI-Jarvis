package com.notegather.biz.domain.knowledge.aggregate;

import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.valueobject.KnowledgeBaseId;
import com.notegather.biz.domain.knowledge.valueobject.KnowledgeBaseStatus;
import com.notegather.biz.domain.knowledge.valueobject.Visibility;

import java.time.LocalDateTime;

/**
 * 知识库聚合根
 * 
 * @author NoteGather
 * @since 1.0.0
 */
public class KnowledgeBase {
    
    private KnowledgeBaseId id;
    private UserId ownerId;
    private String name;
    private String description;
    private String icon;
    private Visibility visibility;
    private KnowledgeBaseStatus status;
    private Integer docCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;
    
    // 私有构造函数，强制使用工厂方法
    private KnowledgeBase() {
    }
    
    /**
     * 创建知识库（工厂方法）
     */
    public static KnowledgeBase create(UserId ownerId, String name, String description, Visibility visibility) {
        if (ownerId == null) {
            throw new IllegalArgumentException("所有者ID不能为空");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库名称不能为空");
        }
        if (name.length() > 128) {
            throw new IllegalArgumentException("知识库名称不能超过128个字符");
        }
        
        KnowledgeBase kb = new KnowledgeBase();
        kb.ownerId = ownerId;
        kb.name = name.trim();
        kb.description = description;
        kb.visibility = visibility != null ? visibility : Visibility.PRIVATE;
        kb.status = KnowledgeBaseStatus.ENABLED;
        kb.docCount = 0;
        kb.deleted = false;
        kb.createdAt = LocalDateTime.now();
        kb.updatedAt = LocalDateTime.now();
        
        return kb;
    }
    
    /**
     * 更新知识库信息
     */
    public void updateInfo(String name, String description, Visibility visibility) {
        if (name != null && !name.trim().isEmpty()) {
            if (name.length() > 128) {
                throw new IllegalArgumentException("知识库名称不能超过128个字符");
            }
            this.name = name.trim();
        }
        
        if (description != null) {
            this.description = description;
        }
        
        if (visibility != null) {
            this.visibility = visibility;
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新图标
     */
    public void updateIcon(String icon) {
        this.icon = icon;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 软删除知识库
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
     * 增加文档计数
     */
    public void incrementDocCount() {
        this.docCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 减少文档计数
     */
    public void decrementDocCount() {
        if (this.docCount > 0) {
            this.docCount--;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * 启用知识库
     */
    public void enable() {
        this.status = KnowledgeBaseStatus.ENABLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 禁用知识库
     */
    public void disable() {
        this.status = KnowledgeBaseStatus.DISABLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters
    public KnowledgeBaseId getId() {
        return id;
    }
    
    public UserId getOwnerId() {
        return ownerId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public Visibility getVisibility() {
        return visibility;
    }
    
    public KnowledgeBaseStatus getStatus() {
        return status;
    }
    
    public Integer getDocCount() {
        return docCount;
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
    public void setId(KnowledgeBaseId id) {
        this.id = id;
    }
    
    public void setOwnerId(UserId ownerId) {
        this.ownerId = ownerId;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
    
    public void setStatus(KnowledgeBaseStatus status) {
        this.status = status;
    }
    
    public void setDocCount(Integer docCount) {
        this.docCount = docCount;
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
