package com.notegather.biz.application.command;

import lombok.Data;

/**
 * 创建文档命令
 */
@Data
public class CreateDocumentCommand {
    
    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;
    
    /**
     * 父文档ID（null表示根目录）
     */
    private Long parentId;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 类型：0-文件夹，1-文档
     */
    private Integer type;
    
    /**
     * 内容（文档类型时必填）
     */
    private String content;
}
