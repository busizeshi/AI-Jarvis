package com.notegather.biz.ai.infrastructure.persistence.session.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notegather.biz.ai.domain.model.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    @Select("SELECT * FROM t_chat_session "
            + "WHERE id = #{sessionId} AND user_id = #{userId} AND deleted = 0 FOR UPDATE")
    ChatSession selectForUpdate(@Param("sessionId") Long sessionId, @Param("userId") Long userId);
}
