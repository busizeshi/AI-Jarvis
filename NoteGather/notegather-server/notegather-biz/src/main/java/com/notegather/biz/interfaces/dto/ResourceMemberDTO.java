package com.notegather.biz.interfaces.dto;

import com.notegather.biz.domain.permission.aggregate.ResourceGrant;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源成员DTO
 */
@Data
public class ResourceMemberDTO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 权限级别
     */
    private String permissionLevel;
    
    /**
     * 权限级别显示名称
     */
    private String permissionLevelName;
    
    /**
     * 授权人ID
     */
    private Long grantedByUserId;
    
    /**
     * 是否继承权限
     */
    private Boolean inherited;
    
    /**
     * 授权时间
     */
    private LocalDateTime grantedAt;
    
    /**
     * 从领域对象转换
     */
    public static ResourceMemberDTO fromDomain(ResourceGrant grant) {
        if (grant == null) {
            return null;
        }
        
        ResourceMemberDTO dto = new ResourceMemberDTO();
        dto.setUserId(grant.getGrantedUserId().getValue());
        dto.setPermissionLevel(grant.getPermissionLevel().getCode());
        dto.setPermissionLevelName(grant.getPermissionLevel().getDisplayName());
        dto.setGrantedByUserId(grant.getGrantedByUserId().getValue());
        dto.setInherited(grant.isInherited());
        dto.setGrantedAt(grant.getCreatedAt());
        
        return dto;
    }
}
