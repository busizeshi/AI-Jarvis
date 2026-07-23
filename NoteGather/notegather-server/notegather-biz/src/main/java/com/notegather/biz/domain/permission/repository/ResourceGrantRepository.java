package com.notegather.biz.domain.permission.repository;

import com.notegather.biz.domain.permission.aggregate.ResourceGrant;
import com.notegather.biz.domain.permission.valueobject.PermissionLevel;
import com.notegather.biz.domain.permission.valueobject.ResourceGrantId;
import com.notegather.biz.domain.permission.valueobject.ResourceId;
import com.notegather.biz.domain.permission.valueobject.ResourceType;
import com.notegather.biz.domain.identity.valueobject.UserId;

import java.util.List;
import java.util.Optional;

/**
 * 资源授权仓储接口
 */
public interface ResourceGrantRepository {
    
    /**
     * 保存资源授权
     */
    ResourceGrant save(ResourceGrant grant);
    
    /**
     * 根据ID查询
     */
    Optional<ResourceGrant> findById(ResourceGrantId id);
    
    /**
     * 根据用户ID和资源查询授权
     */
    Optional<ResourceGrant> findByUserIdAndResource(
            UserId userId,
            ResourceType resourceType,
            ResourceId resourceId);
    
    /**
     * 查询资源的所有授权
     */
    List<ResourceGrant> findByResource(ResourceType resourceType, ResourceId resourceId);
    
    /**
     * 查询资源的指定权限级别的授权列表
     */
    List<ResourceGrant> findByResourceAndPermissionLevel(
            ResourceType resourceType,
            ResourceId resourceId,
            PermissionLevel permissionLevel);
    
    /**
     * 查询用户的所有授权
     */
    List<ResourceGrant> findByUserId(UserId userId);
    
    /**
     * 查询用户对指定资源类型的授权
     */
    List<ResourceGrant> findByUserIdAndResourceType(UserId userId, ResourceType resourceType);
    
    /**
     * 检查授权是否存在
     */
    boolean existsByUserIdAndResource(
            UserId userId,
            ResourceType resourceType,
            ResourceId resourceId);
    
    /**
     * 删除授权
     */
    void delete(ResourceGrantId id);
    
    /**
     * 删除资源的所有授权
     */
    void deleteByResource(ResourceType resourceType, ResourceId resourceId);
    
    /**
     * 删除用户对资源的授权
     */
    void deleteByUserIdAndResource(
            UserId userId,
            ResourceType resourceType,
            ResourceId resourceId);
    
    /**
     * 查询资源的 Owner
     */
    Optional<ResourceGrant> findOwnerByResource(ResourceType resourceType, ResourceId resourceId);
    
    /**
     * 统计资源的成员数量
     */
    long countByResource(ResourceType resourceType, ResourceId resourceId);
}
