package com.notegather.biz.infrastructure.persistence.converter;

import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.aggregate.Document;
import com.notegather.biz.domain.knowledge.valueobject.DocumentId;
import com.notegather.biz.domain.knowledge.valueobject.DocumentStatus;
import com.notegather.biz.domain.knowledge.valueobject.DocumentType;
import com.notegather.biz.domain.knowledge.valueobject.KnowledgeBaseId;
import com.notegather.biz.infrastructure.persistence.entity.DocumentEntity;
import org.springframework.stereotype.Component;

/**
 * 文档领域模型与数据库实体转换器
 */
@Component
public class DocumentConverter {
    
    /**
     * 领域模型转数据库实体
     */
    public DocumentEntity toEntity(Document document) {
        if (document == null) {
            return null;
        }
        
        DocumentEntity entity = new DocumentEntity();
        
        if (document.getId() != null) {
            entity.setId(document.getId().getValue());
        }
        
        entity.setKnowledgeBaseId(document.getKnowledgeBaseId().getValue());
        entity.setParentId(document.getParentId() != null ? document.getParentId().getValue() : null);
        entity.setOwnerId(document.getOwnerId().getValue());
        entity.setTitle(document.getTitle());
        entity.setType(document.getType().getCode());
        entity.setContent(document.getContent());
        entity.setContentType(document.getContentType());
        entity.setFileName(document.getFileName());
        entity.setFileSize(document.getFileSize());
        entity.setFileUrl(document.getFileUrl());
        entity.setDepth(document.getDepth());
        entity.setOrderNum(document.getOrderNum());
        entity.setStatus(document.getStatus().getCode());
        entity.setCurrentVersionId(document.getCurrentVersionId());
        entity.setCreatedAt(document.getCreatedAt());
        entity.setUpdatedAt(document.getUpdatedAt());
        entity.setDeleted(document.isDeleted() ? 1 : 0);
        
        return entity;
    }
    
    /**
     * 数据库实体转领域模型
     */
    public Document toDomain(DocumentEntity entity) {
        if (entity == null) {
            return null;
        }
        
        DocumentType type = DocumentType.fromCode(entity.getType());
        
        Document doc;
        if (type == DocumentType.FOLDER) {
            doc = Document.createFolder(
                KnowledgeBaseId.of(entity.getKnowledgeBaseId()),
                UserId.of(entity.getOwnerId()),
                entity.getParentId() != null ? DocumentId.of(entity.getParentId()) : null,
                entity.getTitle(),
                entity.getDepth()
            );
        } else {
            doc = Document.createDocument(
                KnowledgeBaseId.of(entity.getKnowledgeBaseId()),
                UserId.of(entity.getOwnerId()),
                entity.getParentId() != null ? DocumentId.of(entity.getParentId()) : null,
                entity.getTitle(),
                entity.getContent(),
                entity.getDepth()
            );
        }
        
        // 设置其他属性
        doc.setId(DocumentId.of(entity.getId()));
        doc.setContentType(entity.getContentType());
        doc.setFileName(entity.getFileName());
        doc.setFileSize(entity.getFileSize());
        doc.setFileUrl(entity.getFileUrl());
        doc.setOrderNum(entity.getOrderNum());
        doc.setStatus(DocumentStatus.fromCode(entity.getStatus()));
        doc.setCurrentVersionId(entity.getCurrentVersionId());
        doc.setCreatedAt(entity.getCreatedAt());
        doc.setUpdatedAt(entity.getUpdatedAt());
        doc.setDeleted(entity.getDeleted() == 1);
        
        return doc;
    }
}
