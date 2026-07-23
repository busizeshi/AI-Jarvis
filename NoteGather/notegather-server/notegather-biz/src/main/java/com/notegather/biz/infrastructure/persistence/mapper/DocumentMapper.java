package com.notegather.biz.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notegather.biz.infrastructure.persistence.entity.DocumentEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档 Mapper
 */
@Mapper
public interface DocumentMapper extends BaseMapper<DocumentEntity> {
}
