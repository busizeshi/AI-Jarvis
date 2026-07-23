package com.notegather.biz.infrastructure.persistence.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.notegather.biz.domain.identity.aggregate.User;
import com.notegather.biz.domain.identity.repository.UserRepository;
import com.notegather.biz.domain.identity.valueobject.Email;
import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.infrastructure.persistence.converter.UserConverter;
import com.notegather.biz.infrastructure.persistence.entity.UserEntity;
import com.notegather.biz.infrastructure.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    
    private final UserMapper userMapper;
    
    @Override
    public void save(User user) {
        UserEntity entity = UserConverter.toEntity(user);
        
        if (user.getId() == null) {
            // 新增
            userMapper.insert(entity);
            // MyBatis-Plus 自动回填 ID 到 entity，需同步到领域模型
            user.setId(entity.getId());
        } else {
            // 更新
            userMapper.updateById(entity);
        }
    }
    
    @Override
    public Optional<User> findById(UserId userId) {
        UserEntity entity = userMapper.selectById(userId.getValue());
        return Optional.ofNullable(UserConverter.toDomain(entity));
    }
    
    @Override
    public Optional<User> findByUsername(String username) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username);
        UserEntity entity = userMapper.selectOne(wrapper);
        return Optional.ofNullable(UserConverter.toDomain(entity));
    }
    
    @Override
    public Optional<User> findByEmail(Email email) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getEmail, email.getValue());
        UserEntity entity = userMapper.selectOne(wrapper);
        return Optional.ofNullable(UserConverter.toDomain(entity));
    }
    
    @Override
    public boolean existsByUsername(String username) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUsername, username);
        return userMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public boolean existsByEmail(Email email) {
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getEmail, email.getValue());
        return userMapper.selectCount(wrapper) > 0;
    }
    
    @Override
    public void delete(UserId userId) {
        userMapper.deleteById(userId.getValue());
    }
}
