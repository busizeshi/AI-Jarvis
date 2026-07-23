package com.notegather.biz.application.command;

import com.notegather.biz.domain.permission.valueobject.PermissionLevel;
import com.notegather.biz.domain.permission.valueobject.ResourceType;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 授予权限命令
 */
@Data
public class GrantPermissionCommand {
    
    /**
     * 资源类型
     */
    @NotNull(message = "资源类型不能为空")
    private ResourceType resourceType;
    
    /**
     * 资源ID
     */
    @NotNull(message = "资源ID不能为空")
    private Long resourceId;
    
    /**
     * 被授权的用户ID
     */
    @NotNull(message = "被授权用户ID不能为空")
    private Long grantedUserId;
    
    /**
     * 权限级别
     */
    @NotNull(message = "权限级别不能为空")
    private PermissionLevel permissionLevel;
}
