package com.notegather.biz.interfaces.rest;

import cn.dev33.satoken.stp.StpUtil;
import com.notegather.biz.application.command.GrantPermissionCommand;
import com.notegather.biz.application.command.RevokePermissionCommand;
import com.notegather.biz.application.service.PermissionService;
import com.notegather.biz.domain.permission.aggregate.ResourceGrant;
import com.notegather.biz.domain.permission.valueobject.PermissionLevel;
import com.notegather.biz.domain.permission.valueobject.ResourceType;
import com.notegather.biz.interfaces.dto.PermissionDTO;
import com.notegather.biz.interfaces.dto.ResourceMemberDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限管理REST API
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {
    
    private final PermissionService permissionService;
    
    /**
     * 授予权限
     */
    @PostMapping("/grant")
    public PermissionDTO grantPermission(@Valid @RequestBody GrantPermissionCommand command) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        ResourceGrant grant = permissionService.grantPermission(command, currentUserId);
        return PermissionDTO.fromDomain(grant);
    }
    
    /**
     * 撤销权限
     */
    @PostMapping("/revoke")
    public void revokePermission(@Valid @RequestBody RevokePermissionCommand command) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        permissionService.revokePermission(command, currentUserId);
    }
    
    /**
     * 查询资源的成员列表
     */
    @GetMapping("/members")
    public List<ResourceMemberDTO> listResourceMembers(
            @RequestParam String resourceType,
            @RequestParam Long resourceId) {
        
        Long currentUserId = StpUtil.getLoginIdAsLong();
        ResourceType type = ResourceType.fromCode(resourceType);
        
        List<ResourceGrant> grants = permissionService.listResourceMembers(type, resourceId, currentUserId);
        
        return grants.stream()
                .map(ResourceMemberDTO::fromDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * 查询我的权限列表
     */
    @GetMapping("/my")
    public List<PermissionDTO> listMyPermissions() {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        List<ResourceGrant> grants = permissionService.listUserPermissions(currentUserId);
        
        return grants.stream()
                .map(PermissionDTO::fromDomain)
                .collect(Collectors.toList());
    }
    
    /**
     * 检查权限
     */
    @GetMapping("/check")
    public boolean checkPermission(
            @RequestParam Long userId,
            @RequestParam String resourceType,
            @RequestParam Long resourceId,
            @RequestParam String permissionLevel) {
        
        ResourceType type = ResourceType.fromCode(resourceType);
        PermissionLevel level = PermissionLevel.fromCode(permissionLevel);
        
        return permissionService.checkPermission(userId, type, resourceId, level);
    }
    
    /**
     * 获取用户对资源的权限级别
     */
    @GetMapping("/level")
    public String getUserPermissionLevel(
            @RequestParam Long userId,
            @RequestParam String resourceType,
            @RequestParam Long resourceId) {
        
        ResourceType type = ResourceType.fromCode(resourceType);
        PermissionLevel level = permissionService.getUserPermissionLevel(userId, type, resourceId);
        
        return level != null ? level.getCode() : null;
    }
}
