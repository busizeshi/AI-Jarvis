package com.notegather.biz.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档数据库实体
 */
@Data
@TableName("document")
public class DocumentEntity {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("knowledge_base_id")
    private Long knowledgeBaseId;
    
    @TableField("parent_id")
    private Long parentId;
    
    @TableField("owner_id")
    private Long ownerId;
    
    @TableField("title")
    private String title;
    
    @TableField("type")
    private Integer type;
    
    @TableField("content")
    private String content;
    
    @TableField("content_type")
    private String contentType;
    
    @TableField("file_name")
    private String fileName;
    
    @TableField("file_size")
    private Long fileSize;
    
    @TableField("file_url")
    private String fileUrl;
    
    @TableField("depth")
    private Integer depth;
    
    @TableField("order_num")
    private Integer orderNum;
    
    @TableField("status")
    private Integer status;
    
    @TableField("current_version_id")
    private Long currentVersionId;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
    
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
