package com.notegather.biz.application.command;

import lombok.Data;

/**
 * 移动文档命令
 */
@Data
public class MoveDocumentCommand {
    
    /**
     * 文档ID
     */
    private Long id;
    
    /**
     * 新的父文档ID（null表示移动到根目录）
     */
    private Long newParentId;
}
