package com.notegather.admin.infrastructure.persistence.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notegather.admin.domain.user.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
