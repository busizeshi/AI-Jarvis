package com.notegather.biz.infrastructure.persistence.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档-标签关联表实体
 */
@Data
@TableName("document_tag")
public class DocumentTagEntity {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField("document_id")
    private Long documentId;
    
    @TableField("tag_id")
    private Long tagId;
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}
