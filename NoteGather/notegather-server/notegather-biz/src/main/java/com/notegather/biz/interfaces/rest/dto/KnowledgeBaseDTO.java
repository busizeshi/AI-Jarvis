package com.notegather.biz.interfaces.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库 DTO
 * 
 * @author NoteGather
 * @since 1.0.0
 */
@Data
public class KnowledgeBaseDTO {
    
    private Long id;
    
    private Long ownerId;
    
    private String name;
    
    private String description;
    
    private String icon;
    
    private Integer visibility;
    
    private Integer docCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
