package com.notegather.biz.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notegather.biz.infrastructure.persistence.entity.DocumentTagEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档-标签关联 Mapper
 */
@Mapper
public interface DocumentTagMapper extends BaseMapper<DocumentTagEntity> {
}
