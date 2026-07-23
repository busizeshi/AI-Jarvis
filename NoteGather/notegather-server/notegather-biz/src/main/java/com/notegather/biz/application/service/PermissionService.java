package com.notegather.biz.application.service;

import com.notegather.biz.application.command.GrantPermissionCommand;
import com.notegather.biz.application.command.RevokePermissionCommand;
import com.notegather.biz.domain.permission.aggregate.ResourceGrant;
import com.notegather.biz.domain.permission.repository.ResourceGrantRepository;
import com.notegather.biz.domain.permission.service.PermissionChecker;
import com.notegather.biz.domain.permission.valueobject.*;
import com.notegather.biz.domain.identity.valueobject.UserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 权限管理应用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final ResourceGrantRepository resourceGrantRepository;
    private final PermissionChecker permissionChecker;
    
    /**
     * 授予权限
     */
    @Transactional(rollbackFor = Exception.class)
    public ResourceGrant grantPermission(GrantPermissionCommand command, Long currentUserId) {
        log.info("授予权限: resourceType={}, resourceId={}, userId={}, level={}",
                command.getResourceType(), command.getResourceId(),
                command.getGrantedUserId(), command.getPermissionLevel());
        
        UserId granterUserId = UserId.of(currentUserId);
        ResourceType resourceType = command.getResourceType();
        ResourceId resourceId = ResourceId.of(command.getResourceId());
        UserId grantedUserId = UserId.of(command.getGrantedUserId());
        PermissionLevel permissionLevel = command.getPermissionLevel();
        
        // 检查授权人是否有权限授予
        if (!permissionChecker.canGrant(granterUserId, resourceType, resourceId, permissionLevel)) {
            throw new IllegalStateException("当前用户没有权限进行授权操作");
        }
        
        // 检查是否已存在授权
        boolean exists = resourceGrantRepository.existsByUserIdAndResource(
                grantedUserId, resourceType, resourceId);
        
        if (exists) {
            throw new IllegalStateException("该用户已有此资源的权限");
        }
        
        // 创建授权
        ResourceGrant grant = ResourceGrant.create(
                resourceType,
                resourceId,
                grantedUserId,
                permissionLevel,
                granterUserId
        );
        
        return resourceGrantRepository.save(grant);
    }
    
    /**
     * 撤销权限
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokePermission(RevokePermissionCommand command, Long currentUserId) {
        log.info("撤销权限: resourceType={}, resourceId={}, userId={}",
                command.getResourceType(), command.getResourceId(), command.getUserId());
        
        UserId revokerUserId = UserId.of(currentUserId);
        ResourceType resourceType = command.getResourceType();
        ResourceId resourceId = ResourceId.of(command.getResourceId());
        UserId targetUserId = UserId.of(command.getUserId());
        
        // 查询要撤销的授权
        ResourceGrant grantToRevoke = resourceGrantRepository
                .findByUserIdAndResource(targetUserId, resourceType, resourceId)
                .orElseThrow(() -> new IllegalArgumentException("授权记录不存在"));
        
        log.info("找到授权记录: grantId={}, permissionLevel={}", 
                grantToRevoke.getId().getValue(), grantToRevoke.getPermissionLevel());
        
        // 检查是否有权限撤销
        if (!permissionChecker.canRevoke(revokerUserId, resourceType, resourceId, grantToRevoke)) {
            throw new IllegalStateException("当前用户没有权限撤销此授权");
        }
        
        // 删除授权
        resourceGrantRepository.deleteByUserIdAndResource(targetUserId, resourceType, resourceId);
        log.info("权限已撤销: userId={}, resourceType={}, resourceId={}", 
                targetUserId.getValue(), resourceType, resourceId.getValue());
    }
    
    /**
     * 查询资源的成员列表
     */
    public List<ResourceGrant> listResourceMembers(
            ResourceType resourceType,
            Long resourceId,
            Long currentUserId) {
        
        UserId userId = UserId.of(currentUserId);
        ResourceId resId = ResourceId.of(resourceId);
        
        // 检查是否有查看权限
        permissionChecker.ensurePermission(userId, resourceType, resId, PermissionLevel.VIEWER);
        
        return resourceGrantRepository.findByResource(resourceType, resId);
    }
    
    /**
     * 查询用户的权限列表
     */
    public List<ResourceGrant> listUserPermissions(Long userId) {
        return resourceGrantRepository.findByUserId(UserId.of(userId));
    }
    
    /**
     * 检查用户是否有权限
     */
    public boolean checkPermission(
            Long userId,
            ResourceType resourceType,
            Long resourceId,
            PermissionLevel requiredLevel) {
        
        log.info("检查权限: userId={}, resourceType={}, resourceId={}, requiredLevel={}",
                userId, resourceType, resourceId, requiredLevel);
        
        boolean hasPermission = permissionChecker.hasPermission(
                UserId.of(userId),
                resourceType,
                ResourceId.of(resourceId),
                requiredLevel
        );
        
        log.info("权限检查结果: hasPermission={}", hasPermission);
        return hasPermission;
    }
    
    /**
     * 获取用户对资源的权限级别
     */
    public PermissionLevel getUserPermissionLevel(
            Long userId,
            ResourceType resourceType,
            Long resourceId) {
        
        return permissionChecker.getPermissionLevel(
                UserId.of(userId),
                resourceType,
                ResourceId.of(resourceId)
        ).orElse(null);
    }
}
