package com.notegather.biz.ai.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.notegather.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("t_chat_session")
@EqualsAndHashCode(callSuper = true)
public class ChatSession extends BaseEntity {

    private Long userId;
    private String title;
}
