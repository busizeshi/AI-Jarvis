package com.notegather.biz.ai.domain.model;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_chat_citation")
public class ChatCitation {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long messageId;
    private Long noteId;
    private Integer citationOrder;
    private String noteTitle;
    private String chunkText;
    private Float score;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
