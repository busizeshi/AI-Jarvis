package com.notegather.biz.application.command;

import lombok.Data;

/**
 * 更新文档命令
 */
@Data
public class UpdateDocumentCommand {
    
    /**
     * 文档ID
     */
    private Long id;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容
     */
    private String content;
}
