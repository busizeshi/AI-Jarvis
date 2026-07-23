package com.notegather.biz.interfaces.dto;

import com.notegather.biz.domain.permission.aggregate.ResourceGrant;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限DTO
 */
@Data
public class PermissionDTO {
    
    /**
     * 授权ID
     */
    private Long id;
    
    /**
     * 资源类型
     */
    private String resourceType;
    
    /**
     * 资源ID
     */
    private Long resourceId;
    
    /**
     * 被授权的用户ID
     */
    private Long grantedUserId;
    
    /**
     * 权限级别
     */
    private String permissionLevel;
    
    /**
     * 授权人ID
     */
    private Long grantedByUserId;
    
    /**
     * 是否继承权限
     */
    private Boolean inherited;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 从领域对象转换
     */
    public static PermissionDTO fromDomain(ResourceGrant grant) {
        if (grant == null) {
            return null;
        }
        
        PermissionDTO dto = new PermissionDTO();
        
        if (grant.getId() != null) {
            dto.setId(grant.getId().getValue());
        }
        
        dto.setResourceType(grant.getResourceType().getCode());
        dto.setResourceId(grant.getResourceId().getValue());
        dto.setGrantedUserId(grant.getGrantedUserId().getValue());
        dto.setPermissionLevel(grant.getPermissionLevel().getCode());
        dto.setGrantedByUserId(grant.getGrantedByUserId().getValue());
        dto.setInherited(grant.isInherited());
        dto.setCreatedAt(grant.getCreatedAt());
        
        return dto;
    }
}
