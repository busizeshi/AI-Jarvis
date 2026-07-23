package com.notegather.biz.domain.knowledge.repository;

import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.aggregate.KnowledgeBase;
import com.notegather.biz.domain.knowledge.valueobject.KnowledgeBaseId;

import java.util.List;
import java.util.Optional;

/**
 * 知识库仓储接口
 * 
 * @author NoteGather
 * @since 1.0.0
 */
public interface KnowledgeBaseRepository {
    
    /**
     * 保存知识库
     */
    KnowledgeBase save(KnowledgeBase knowledgeBase);
    
    /**
     * 根据ID查询知识库
     */
    Optional<KnowledgeBase> findById(KnowledgeBaseId id);
    
    /**
     * 根据所有者ID查询知识库列表
     */
    List<KnowledgeBase> findByOwnerId(UserId ownerId);
    
    /**
     * 根据所有者ID分页查询知识库
     */
    List<KnowledgeBase> findByOwnerIdWithPage(UserId ownerId, int page, int size);
    
    /**
     * 统计所有者的知识库数量
     */
    long countByOwnerId(UserId ownerId);
    
    /**
     * 删除知识库（物理删除，谨慎使用）
     */
    void deleteById(KnowledgeBaseId id);
}
