package com.notegather.biz.ai.infrastructure.persistence.citation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notegather.biz.ai.domain.model.ChatCitation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatCitationMapper extends BaseMapper<ChatCitation> {
}
