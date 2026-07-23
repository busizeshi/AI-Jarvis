package com.notegather.biz.domain.permission.service;

import com.notegather.biz.domain.permission.aggregate.ResourceGrant;
import com.notegather.biz.domain.permission.repository.ResourceGrantRepository;
import com.notegather.biz.domain.permission.valueobject.PermissionLevel;
import com.notegather.biz.domain.permission.valueobject.ResourceId;
import com.notegather.biz.domain.permission.valueobject.ResourceType;
import com.notegather.biz.domain.identity.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 权限检查器领域服务
 * 负责检查用户对资源的访问权限
 */
@Service
@RequiredArgsConstructor
public class PermissionChecker {
    
    private final ResourceGrantRepository resourceGrantRepository;
    
    /**
     * 检查用户是否有资源的指定权限
     */
    public boolean hasPermission(
            UserId userId,
            ResourceType resourceType,
            ResourceId resourceId,
            PermissionLevel requiredLevel) {
        
        if (userId == null || resourceType == null || resourceId == null || requiredLevel == null) {
            return false;
        }
        
        Optional<ResourceGrant> grantOpt = resourceGrantRepository
                .findByUserIdAndResource(userId, resourceType, resourceId);
        
        if (grantOpt.isEmpty()) {
            return false;
        }
        
        ResourceGrant grant = grantOpt.get();
        return grant.hasPermission(requiredLevel);
    }
    
    /**
     * 检查用户是否为资源的 Owner
     */
    public boolean isOwner(UserId userId, ResourceType resourceType, ResourceId resourceId) {
        return hasPermission(userId, resourceType, resourceId, PermissionLevel.OWNER);
    }
    
    /**
     * 检查用户是否可以编辑资源
     */
    public boolean canEdit(UserId userId, ResourceType resourceType, ResourceId resourceId) {
        return hasPermission(userId, resourceType, resourceId, PermissionLevel.EDITOR);
    }
    
    /**
     * 检查用户是否可以查看资源
     */
    public boolean canView(UserId userId, ResourceType resourceType, ResourceId resourceId) {
        return hasPermission(userId, resourceType, resourceId, PermissionLevel.VIEWER);
    }
    
    /**
     * 获取用户对资源的权限级别
     */
    public Optional<PermissionLevel> getPermissionLevel(
            UserId userId,
            ResourceType resourceType,
            ResourceId resourceId) {
        
        return resourceGrantRepository
                .findByUserIdAndResource(userId, resourceType, resourceId)
                .map(ResourceGrant::getPermissionLevel);
    }
    
    /**
     * 检查用户是否可以授予指定权限
     * 规则：只有 Owner 可以授权
     */
    public boolean canGrant(
            UserId granterId,
            ResourceType resourceType,
            ResourceId resourceId,
            PermissionLevel levelToGrant) {
        
        return isOwner(granterId, resourceType, resourceId);
    }
    
    /**
     * 检查用户是否可以撤销指定授权
     * 规则：Owner 可以撤销任何授权（除了自己的 Owner 权限）
     */
    public boolean canRevoke(
            UserId revokerId,
            ResourceType resourceType,
            ResourceId resourceId,
            ResourceGrant grantToRevoke) {
        
        if (!isOwner(revokerId, resourceType, resourceId)) {
            return false;
        }
        
        if (grantToRevoke.isGrantedTo(revokerId) && grantToRevoke.isOwner()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 确保用户有权限，否则抛出异常
     */
    public void ensurePermission(
            UserId userId,
            ResourceType resourceType,
            ResourceId resourceId,
            PermissionLevel requiredLevel) {
        
        if (!hasPermission(userId, resourceType, resourceId, requiredLevel)) {
            throw new PermissionDeniedException(
                    String.format("用户 %s 没有资源 %s:%s 的 %s 权限",
                            userId.getValue(),
                            resourceType.getCode(),
                            resourceId.getValue(),
                            requiredLevel.getDisplayName())
            );
        }
    }
    
    /**
     * 权限拒绝异常
     */
    public static class PermissionDeniedException extends RuntimeException {
        public PermissionDeniedException(String message) {
            super(message);
        }
    }
}
