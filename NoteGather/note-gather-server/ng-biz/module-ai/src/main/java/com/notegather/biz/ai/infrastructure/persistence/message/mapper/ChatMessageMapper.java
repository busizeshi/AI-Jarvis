package com.notegather.biz.ai.infrastructure.persistence.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notegather.biz.ai.domain.model.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT COALESCE(MAX(message_no), 0) FROM t_chat_message "
            + "WHERE session_id = #{sessionId} AND deleted = 0")
    Integer selectCurrentMessageNo(@Param("sessionId") Long sessionId);
}
