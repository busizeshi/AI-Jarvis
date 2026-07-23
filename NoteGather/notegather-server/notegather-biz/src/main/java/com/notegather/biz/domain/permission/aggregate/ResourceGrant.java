package com.notegather.biz.domain.permission.aggregate;

import com.notegather.biz.domain.permission.valueobject.*;
import com.notegather.biz.domain.identity.valueobject.UserId;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 资源授权聚合根
 * 管理资源的访问权限（Owner/Editor/Companion/Viewer）
 */
@Getter
public class ResourceGrant {
    
    private ResourceGrantId id;
    
    /**
     * 资源类型（知识库或文档）
     */
    private ResourceType resourceType;
    
    /**
     * 资源ID
     */
    private ResourceId resourceId;
    
    /**
     * 被授权的用户ID
     */
    private UserId grantedUserId;
    
    /**
     * 权限级别
     */
    private PermissionLevel permissionLevel;
    
    /**
     * 授权人ID
     */
    private UserId grantedByUserId;
    
    /**
     * 是否继承权限（从知识库继承到文档）
     */
    private boolean inherited;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 创建资源授权（显式授权）
     */
    public static ResourceGrant create(
            ResourceType resourceType,
            ResourceId resourceId,
            UserId grantedUserId,
            PermissionLevel permissionLevel,
            UserId grantedByUserId) {
        
        // 参数校验
        if (resourceType == null) {
            throw new IllegalArgumentException("资源类型不能为空");
        }
        if (resourceId == null) {
            throw new IllegalArgumentException("资源ID不能为空");
        }
        if (grantedUserId == null) {
            throw new IllegalArgumentException("被授权用户ID不能为空");
        }
        if (permissionLevel == null) {
            throw new IllegalArgumentException("权限级别不能为空");
        }
        if (grantedByUserId == null) {
            throw new IllegalArgumentException("授权人ID不能为空");
        }
        
        ResourceGrant grant = new ResourceGrant();
        grant.resourceType = resourceType;
        grant.resourceId = resourceId;
        grant.grantedUserId = grantedUserId;
        grant.permissionLevel = permissionLevel;
        grant.grantedByUserId = grantedByUserId;
        grant.inherited = false;
        grant.createdAt = LocalDateTime.now();
        grant.updatedAt = LocalDateTime.now();
        
        return grant;
    }
    
    /**
     * 创建继承权限（从知识库继承到文档）
     */
    public static ResourceGrant createInherited(
            ResourceId documentId,
            UserId grantedUserId,
            PermissionLevel permissionLevel,
            UserId grantedByUserId) {
        
        ResourceGrant grant = new ResourceGrant();
        grant.resourceType = ResourceType.DOCUMENT;
        grant.resourceId = documentId;
        grant.grantedUserId = grantedUserId;
        grant.permissionLevel = permissionLevel;
        grant.grantedByUserId = grantedByUserId;
        grant.inherited = true;
        grant.createdAt = LocalDateTime.now();
        grant.updatedAt = LocalDateTime.now();
        
        return grant;
    }
    
    /**
     * 从数据库重建
     */
    public static ResourceGrant reconstruct(
            ResourceGrantId id,
            ResourceType resourceType,
            ResourceId resourceId,
            UserId grantedUserId,
            PermissionLevel permissionLevel,
            UserId grantedByUserId,
            boolean inherited,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        
        ResourceGrant grant = new ResourceGrant();
        grant.id = id;
        grant.resourceType = resourceType;
        grant.resourceId = resourceId;
        grant.grantedUserId = grantedUserId;
        grant.permissionLevel = permissionLevel;
        grant.grantedByUserId = grantedByUserId;
        grant.inherited = inherited;
        grant.createdAt = createdAt;
        grant.updatedAt = updatedAt;
        
        return grant;
    }
    
    /**
     * 更新权限级别
     * 只有非继承权限才能更新
     */
    public void updatePermissionLevel(PermissionLevel newLevel, UserId updaterUserId) {
        if (this.inherited) {
            throw new IllegalStateException("继承权限不能直接更新");
        }
        if (newLevel == null) {
            throw new IllegalArgumentException("新权限级别不能为空");
        }
        
        this.permissionLevel = newLevel;
        this.grantedByUserId = updaterUserId;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查是否有指定权限级别
     */
    public boolean hasPermission(PermissionLevel requiredLevel) {
        return this.permissionLevel.isAtLeast(requiredLevel);
    }
    
    /**
     * 检查是否为 Owner
     */
    public boolean isOwner() {
        return this.permissionLevel.isOwner();
    }
    
    /**
     * 检查是否可以编辑
     */
    public boolean canEdit() {
        return this.permissionLevel.canEdit();
    }
    
    /**
     * 检查是否为指定用户的授权
     */
    public boolean isGrantedTo(UserId userId) {
        return this.grantedUserId.equals(userId);
    }
    
    /**
     * 检查授权人是否为指定用户
     */
    public boolean isGrantedBy(UserId userId) {
        return this.grantedByUserId.equals(userId);
    }
}
