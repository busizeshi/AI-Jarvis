package com.notegather.biz.file.infrastructure.persistence.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notegather.biz.file.domain.model.FileRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper extends BaseMapper<FileRecord> {
}
