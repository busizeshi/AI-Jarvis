package com.notegather.biz.domain.knowledge.repository;

import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.aggregate.Tag;
import com.notegather.biz.domain.knowledge.valueobject.DocumentId;
import com.notegather.biz.domain.knowledge.valueobject.TagId;

import java.util.List;
import java.util.Optional;

/**
 * 标签仓储接口
 */
public interface TagRepository {
    
    /**
     * 保存标签
     */
    Tag save(Tag tag);
    
    /**
     * 根据ID查询标签
     */
    Optional<Tag> findById(TagId tagId);
    
    /**
     * 根据用户ID查询所有标签
     */
    List<Tag> findByOwnerId(UserId ownerId);
    
    /**
     * 根据名称和用户ID查询标签
     */
    Optional<Tag> findByOwnerIdAndName(UserId ownerId, String name);
    
    /**
     * 检查标签名称是否存在（同一用户下）
     */
    boolean existsByOwnerIdAndName(UserId ownerId, String name);
    
    /**
     * 删除标签
     */
    void delete(TagId tagId);
    
    /**
     * 为文档添加标签
     */
    void attachToDocument(DocumentId documentId, TagId tagId);
    
    /**
     * 从文档移除标签
     */
    void detachFromDocument(DocumentId documentId, TagId tagId);
    
    /**
     * 查询文档的所有标签
     */
    List<Tag> findByDocumentId(DocumentId documentId);
    
    /**
     * 查询使用该标签的文档ID列表
     */
    List<Long> findDocumentIdsByTagId(TagId tagId);
    
    /**
     * 批量删除文档的所有标签
     */
    void deleteAllByDocumentId(DocumentId documentId);
}
