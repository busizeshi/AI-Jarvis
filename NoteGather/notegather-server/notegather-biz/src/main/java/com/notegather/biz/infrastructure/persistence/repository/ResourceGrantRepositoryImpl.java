package com.notegather.biz.infrastructure.persistence.repository;

import com.notegather.biz.domain.permission.aggregate.ResourceGrant;
import com.notegather.biz.domain.permission.repository.ResourceGrantRepository;
import com.notegather.biz.domain.permission.valueobject.PermissionLevel;
import com.notegather.biz.domain.permission.valueobject.ResourceGrantId;
import com.notegather.biz.domain.permission.valueobject.ResourceId;
import com.notegather.biz.domain.permission.valueobject.ResourceType;
import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.infrastructure.persistence.converter.ResourceGrantConverter;
import com.notegather.biz.infrastructure.persistence.entity.ResourceGrantEntity;
import com.notegather.biz.infrastructure.persistence.mapper.ResourceGrantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 资源授权仓储实现
 */
@Repository
@RequiredArgsConstructor
public class ResourceGrantRepositoryImpl implements ResourceGrantRepository {
    
    private final ResourceGrantMapper mapper;
    private final ResourceGrantConverter converter;
    
    @Override
    public ResourceGrant save(ResourceGrant grant) {
        ResourceGrantEntity entity = converter.toEntity(grant);
        
        if (entity.getId() == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        
        return converter.toDomain(entity);
    }
    
    @Override
    public Optional<ResourceGrant> findById(ResourceGrantId id) {
        ResourceGrantEntity entity = mapper.selectById(id.getValue());
        return Optional.ofNullable(converter.toDomain(entity));
    }
    
    @Override
    public Optional<ResourceGrant> findByUserIdAndResource(
            UserId userId,
            ResourceType resourceType,
            ResourceId resourceId) {
        
        ResourceGrantEntity entity = mapper.selectByUserIdAndResource(
                userId.getValue(),
                resourceType.getCode(),
                resourceId.getValue()
        );
        
        return Optional.ofNullable(converter.toDomain(entity));
    }
    
    @Override
    public List<ResourceGrant> findByResource(ResourceType resourceType, ResourceId resourceId) {
        List<ResourceGrantEntity> entities = mapper.selectByResource(
                resourceType.getCode(),
                resourceId.getValue()
        );
        
        return entities.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ResourceGrant> findByResourceAndPermissionLevel(
            ResourceType resourceType,
            ResourceId resourceId,
            PermissionLevel permissionLevel) {
        
        List<ResourceGrantEntity> entities = mapper.selectByResourceAndPermissionLevel(
                resourceType.getCode(),
                resourceId.getValue(),
                permissionLevel.getCode()
        );
        
        return entities.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ResourceGrant> findByUserId(UserId userId) {
        List<ResourceGrantEntity> entities = mapper.selectByUserId(userId.getValue());
        
        return entities.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ResourceGrant> findByUserIdAndResourceType(UserId userId, ResourceType resourceType) {
        List<ResourceGrantEntity> entities = mapper.selectByUserIdAndResourceType(
                userId.getValue(),
                resourceType.getCode()
        );
        
        return entities.stream()
                .map(converter::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByUserIdAndResource(
            UserId userId,
            ResourceType resourceType,
            ResourceId resourceId) {
        
        int count = mapper.countByUserIdAndResource(
                userId.getValue(),
                resourceType.getCode(),
                resourceId.getValue()
        );
        
        return count > 0;
    }
    
    @Override
    public void delete(ResourceGrantId id) {
        mapper.deleteById(id.getValue());
    }
    
    @Override
    public void deleteByResource(ResourceType resourceType, ResourceId resourceId) {
        mapper.deleteByResource(
                resourceType.getCode(),
                resourceId.getValue()
        );
    }
    
    @Override
    public void deleteByUserIdAndResource(
            UserId userId,
            ResourceType resourceType,
            ResourceId resourceId) {
        
        mapper.deleteByUserIdAndResource(
                userId.getValue(),
                resourceType.getCode(),
                resourceId.getValue()
        );
    }
    
    @Override
    public Optional<ResourceGrant> findOwnerByResource(ResourceType resourceType, ResourceId resourceId) {
        ResourceGrantEntity entity = mapper.selectOwnerByResource(
                resourceType.getCode(),
                resourceId.getValue()
        );
        
        return Optional.ofNullable(converter.toDomain(entity));
    }
    
    @Override
    public long countByResource(ResourceType resourceType, ResourceId resourceId) {
        return mapper.countByResource(
                resourceType.getCode(),
                resourceId.getValue()
        );
    }
}
