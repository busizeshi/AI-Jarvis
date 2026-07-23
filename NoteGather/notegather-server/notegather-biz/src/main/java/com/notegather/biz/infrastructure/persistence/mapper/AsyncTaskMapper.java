package com.notegather.biz.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notegather.biz.infrastructure.persistence.entity.AsyncTaskEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异步任务 Mapper
 */
@Mapper
public interface AsyncTaskMapper extends BaseMapper<AsyncTaskEntity> {
}
