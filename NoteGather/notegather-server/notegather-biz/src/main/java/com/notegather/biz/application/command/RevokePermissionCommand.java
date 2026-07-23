package com.notegather.biz.application.command;

import com.notegather.biz.domain.permission.valueobject.ResourceType;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 撤销权限命令
 */
@Data
public class RevokePermissionCommand {
    
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
     * 被撤销权限的用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
