package com.notegather.biz.file.infrastructure.persistence.parsetask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notegather.biz.file.domain.model.ParseTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ParseTaskMapper extends BaseMapper<ParseTask> {
}
