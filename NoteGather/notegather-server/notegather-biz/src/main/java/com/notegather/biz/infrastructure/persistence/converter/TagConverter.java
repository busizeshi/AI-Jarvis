package com.notegather.biz.infrastructure.persistence.converter;

import com.notegather.biz.domain.knowledge.aggregate.Tag;
import com.notegather.biz.infrastructure.persistence.entity.TagEntity;
import org.springframework.stereotype.Component;

/**
 * 标签领域模型与数据库实体转换器
 */
@Component
public class TagConverter {
    
    /**
     * 数据库实体转领域模型
     */
    public Tag toDomain(TagEntity entity) {
        if (entity == null) {
            return null;
        }
        return Tag.reconstitute(
            entity.getId(),
            entity.getOwnerId(),
            entity.getName(),
            entity.getColor(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getDeleted() != null && entity.getDeleted() == 1
        );
    }
    
    /**
     * 领域模型转数据库实体
     */
    public TagEntity toEntity(Tag tag) {
        if (tag == null) {
            return null;
        }
        
        TagEntity entity = new TagEntity();
        
        // 只有在更新时才设置ID
        if (tag.getId() != null) {
            entity.setId(tag.getId().getValue());
        }
        
        entity.setOwnerId(tag.getOwnerId().getValue());
        entity.setName(tag.getName());
        entity.setColor(tag.getColor());
        entity.setCreatedAt(tag.getCreatedAt());
        entity.setUpdatedAt(tag.getUpdatedAt());
        entity.setDeleted(tag.isDeleted() ? 1 : 0);
        
        return entity;
    }
}
