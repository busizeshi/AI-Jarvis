package com.notegather.biz.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档 DTO
 */
@Data
public class DocumentDTO {
    
    private Long id;
    
    private Long knowledgeBaseId;
    
    private Long parentId;
    
    private Long ownerId;
    
    private String title;
    
    private Integer type;
    
    private String content;
    
    private String contentType;
    
    private String fileName;
    
    private Long fileSize;
    
    private String fileUrl;
    
    private Integer depth;
    
    private Integer orderNum;
    
    private Integer status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
