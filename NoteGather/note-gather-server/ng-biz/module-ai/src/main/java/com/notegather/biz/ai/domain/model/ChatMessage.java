package com.notegather.biz.ai.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.notegather.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_chat_message")
@EqualsAndHashCode(callSuper = true)
public class ChatMessage extends BaseEntity {

    private Long sessionId;
    private Integer messageNo;
    private String role;
    private String content;
    private String status;
    private String errorMsg;
}
