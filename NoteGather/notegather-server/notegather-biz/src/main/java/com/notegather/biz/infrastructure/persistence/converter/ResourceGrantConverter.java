package com.notegather.biz.infrastructure.persistence.converter;

import com.notegather.biz.domain.permission.aggregate.ResourceGrant;
import com.notegather.biz.domain.permission.valueobject.*;
import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.infrastructure.persistence.entity.ResourceGrantEntity;
import org.springframework.stereotype.Component;

/**
 * 资源授权转换器
 */
@Component
public class ResourceGrantConverter {
    
    /**
     * 实体转领域对象
     */
    public ResourceGrant toDomain(ResourceGrantEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return ResourceGrant.reconstruct(
                ResourceGrantId.reconstruct(entity.getId()),
                ResourceType.fromCode(entity.getResourceType()),
                ResourceId.reconstruct(entity.getResourceId()),
                UserId.reconstruct(entity.getGrantedUserId()),
                PermissionLevel.fromCode(entity.getPermissionLevel()),
                UserId.reconstruct(entity.getGrantedByUserId()),
                entity.getInherited(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
    
    /**
     * 领域对象转实体
     */
    public ResourceGrantEntity toEntity(ResourceGrant grant) {
        if (grant == null) {
            return null;
        }
        
        ResourceGrantEntity entity = new ResourceGrantEntity();
        
        if (grant.getId() != null) {
            entity.setId(grant.getId().getValue());
        }
        
        entity.setResourceType(grant.getResourceType().getCode());
        entity.setResourceId(grant.getResourceId().getValue());
        entity.setGrantedUserId(grant.getGrantedUserId().getValue());
        entity.setPermissionLevel(grant.getPermissionLevel().getCode());
        entity.setGrantedByUserId(grant.getGrantedByUserId().getValue());
        entity.setInherited(grant.isInherited());
        entity.setCreatedAt(grant.getCreatedAt());
        entity.setUpdatedAt(grant.getUpdatedAt());
        
        return entity;
    }
}
