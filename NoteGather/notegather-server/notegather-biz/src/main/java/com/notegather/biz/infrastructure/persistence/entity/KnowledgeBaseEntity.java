package com.notegather.biz.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库数据库实体
 * 
 * @author NoteGather
 * @since 1.0.0
 */
@Data
@TableName("knowledge_base")
public class KnowledgeBaseEntity {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("owner_id")
    private Long ownerId;
    
    @TableField("name")
    private String name;
    
    @TableField("description")
    private String description;
    
    @TableField("icon")
    private String icon;
    
    @TableField("visibility")
    private Integer visibility;
    
    @TableField("doc_count")
    private Integer docCount;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
