package com.notegather.biz.infrastructure.persistence.converter;

import com.notegather.biz.domain.knowledge.entity.Favorite;
import com.notegather.biz.domain.knowledge.valueobject.ResourceType;
import com.notegather.biz.infrastructure.persistence.entity.FavoriteEntity;
import org.springframework.stereotype.Component;

/**
 * 收藏领域模型与数据库实体转换器
 */
@Component
public class FavoriteConverter {
    
    /**
     * 数据库实体转领域模型
     */
    public Favorite toDomain(FavoriteEntity entity) {
        if (entity == null) {
            return null;
        }
        return Favorite.reconstitute(
            entity.getId(),
            entity.getUserId(),
            ResourceType.fromCode(entity.getResourceType()),
            entity.getResourceId(),
            entity.getCreatedAt()
        );
    }
    
    /**
     * 领域模型转数据库实体
     */
    public FavoriteEntity toEntity(Favorite favorite) {
        if (favorite == null) {
            return null;
        }
        
        FavoriteEntity entity = new FavoriteEntity();
        
        if (favorite.getId() != null) {
            entity.setId(favorite.getId().getValue());
        }
        
        entity.setUserId(favorite.getUserId().getValue());
        entity.setResourceType(favorite.getResourceType().getCode());
        entity.setResourceId(favorite.getResourceId());
        entity.setCreatedAt(favorite.getCreatedAt());
        
        return entity;
    }
}
