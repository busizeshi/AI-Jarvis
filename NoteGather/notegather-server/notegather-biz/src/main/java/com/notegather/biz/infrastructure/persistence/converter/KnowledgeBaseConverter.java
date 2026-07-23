package com.notegather.biz.infrastructure.persistence.converter;

import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.aggregate.KnowledgeBase;
import com.notegather.biz.domain.knowledge.valueobject.KnowledgeBaseId;
import com.notegather.biz.domain.knowledge.valueobject.KnowledgeBaseStatus;
import com.notegather.biz.domain.knowledge.valueobject.Visibility;
import com.notegather.biz.infrastructure.persistence.entity.KnowledgeBaseEntity;
import org.springframework.stereotype.Component;

/**
 * 知识库领域模型与数据库实体转换器
 * 
 * @author NoteGather
 * @since 1.0.0
 */
@Component
public class KnowledgeBaseConverter {
    
    /**
     * 领域模型转数据库实体
     */
    public KnowledgeBaseEntity toEntity(KnowledgeBase knowledgeBase) {
        if (knowledgeBase == null) {
            return null;
        }
        
        KnowledgeBaseEntity entity = new KnowledgeBaseEntity();
        
        // 只有在更新时才设置ID
        if (knowledgeBase.getId() != null) {
            entity.setId(knowledgeBase.getId().getValue());
        }
        
        entity.setOwnerId(knowledgeBase.getOwnerId().getValue());
        entity.setName(knowledgeBase.getName());
        entity.setDescription(knowledgeBase.getDescription());
        entity.setIcon(knowledgeBase.getIcon());
        entity.setVisibility(knowledgeBase.getVisibility().getCode());
        entity.setDocCount(knowledgeBase.getDocCount());
        entity.setCreatedAt(knowledgeBase.getCreatedAt());
        entity.setUpdatedAt(knowledgeBase.getUpdatedAt());
        entity.setDeleted(knowledgeBase.isDeleted() ? 1 : 0);
        
        return entity;
    }
    
    /**
     * 数据库实体转领域模型
     */
    public KnowledgeBase toDomain(KnowledgeBaseEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // 使用反射或工厂方法创建领域对象
        KnowledgeBase kb = KnowledgeBase.create(
            UserId.of(entity.getOwnerId()),
            entity.getName(),
            entity.getDescription(),
            Visibility.fromCode(entity.getVisibility())
        );
        
        // 设置其他属性（使用包级访问的setter）
        kb.setId(KnowledgeBaseId.of(entity.getId()));
        kb.setIcon(entity.getIcon());
        kb.setDocCount(entity.getDocCount());
        kb.setCreatedAt(entity.getCreatedAt());
        kb.setUpdatedAt(entity.getUpdatedAt());
        kb.setDeleted(entity.getDeleted() == 1);
        
        return kb;
    }
}
