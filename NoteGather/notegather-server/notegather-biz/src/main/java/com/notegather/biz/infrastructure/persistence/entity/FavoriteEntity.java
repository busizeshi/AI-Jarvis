package com.notegather.biz.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏数据库实体
 */
@Data
@TableName("favorite")
public class FavoriteEntity {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("user_id")
    private Long userId;
    
    @TableField("resource_type")
    private String resourceType;
    
    @TableField("resource_id")
    private Long resourceId;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}
