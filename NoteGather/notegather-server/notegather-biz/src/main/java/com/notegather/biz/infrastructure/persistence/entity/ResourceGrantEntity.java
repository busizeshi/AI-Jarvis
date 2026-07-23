package com.notegather.biz.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 资源授权实体
 */
@Data
@TableName("resource_grants")
public class ResourceGrantEntity {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 资源类型（knowledge_base/document）
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
     * 权限级别（owner/editor/companion/viewer）
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
