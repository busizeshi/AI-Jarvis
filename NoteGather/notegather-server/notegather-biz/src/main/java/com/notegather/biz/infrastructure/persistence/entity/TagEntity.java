package com.notegather.biz.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签数据库实体
 */
@Data
@TableName("tag")
public class TagEntity {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("owner_id")
    private Long ownerId;
    
    @TableField("name")
    private String name;
    
    @TableField("color")
    private String color;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
