package com.notegather.biz.application.command;

import lombok.Data;

/**
 * 创建知识库命令
 * 
 * @author NoteGather
 * @since 1.0.0
 */
@Data
public class CreateKnowledgeBaseCommand {
    
    /**
     * 知识库名称
     */
    private String name;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 可见性：0-私有，1-团队，2-公开
     */
    private Integer visibility;
}
