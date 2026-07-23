package com.notegather.biz.domain.knowledge.repository;

import com.notegather.biz.domain.knowledge.aggregate.Document;
import com.notegather.biz.domain.knowledge.valueobject.DocumentId;
import com.notegather.biz.domain.knowledge.valueobject.KnowledgeBaseId;

import java.util.List;
import java.util.Optional;

/**
 * 文档仓储接口
 */
public interface DocumentRepository {
    
    /**
     * 保存文档
     */
    Document save(Document document);
    
    /**
     * 根据ID查询文档
     */
    Optional<Document> findById(DocumentId id);
    
    /**
     * 根据知识库ID查询所有文档
     */
    List<Document> findByKnowledgeBaseId(KnowledgeBaseId knowledgeBaseId);
    
    /**
     * 根据父ID查询子文档
     */
    List<Document> findByParentId(DocumentId parentId);
    
    /**
     * 根据知识库ID和父ID查询文档
     */
    List<Document> findByKnowledgeBaseIdAndParentId(KnowledgeBaseId knowledgeBaseId, DocumentId parentId);
    
    /**
     * 查询根目录文档（parent_id为null的文档）
     */
    List<Document> findRootDocuments(KnowledgeBaseId knowledgeBaseId);
    
    /**
     * 删除文档
     */
    void deleteById(DocumentId id);
}
