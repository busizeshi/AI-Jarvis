package com.notegather.biz.infrastructure.persistence.converter;

import com.notegather.biz.domain.identity.aggregate.User;
import com.notegather.biz.domain.identity.valueobject.Email;
import com.notegather.biz.infrastructure.persistence.entity.UserEntity;

/**
 * 用户领域模型与实体转换器
 */
public class UserConverter {
    
    /**
     * 实体转领域模型
     */
    public static User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.reconstitute(
            entity.getId(),
            entity.getUsername(),
            entity.getEmail(),
            entity.getPasswordHash(),
            entity.getDisplayName(),
            entity.getAvatarUrl(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getDeleted() != null && entity.getDeleted()
        );
    }
    
    /**
     * 领域模型转实体
     */
    public static UserEntity toEntity(User user) {
        if (user == null) {
            return null;
        }
        
        UserEntity entity = new UserEntity();
        
        // ID（新增时为null）
        if (user.getId() != null) {
            entity.setId(user.getId().getValue());
        }
        
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail().getValue());
        entity.setPasswordHash(user.getPassword().getHash());
        entity.setDisplayName(user.getDisplayName());
        entity.setAvatarUrl(user.getAvatarUrl());
        entity.setStatus(user.getStatus().getCode());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setDeleted(user.isDeleted());
        
        return entity;
    }
}
