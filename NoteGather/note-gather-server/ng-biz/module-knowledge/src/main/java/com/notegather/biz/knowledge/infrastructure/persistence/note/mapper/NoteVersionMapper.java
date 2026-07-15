package com.notegather.biz.knowledge.infrastructure.persistence.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notegather.biz.knowledge.domain.model.NoteVersion;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoteVersionMapper extends BaseMapper<NoteVersion> {
}
