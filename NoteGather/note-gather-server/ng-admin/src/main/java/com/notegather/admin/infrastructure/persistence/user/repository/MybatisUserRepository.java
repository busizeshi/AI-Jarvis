package com.notegather.admin.infrastructure.persistence.user.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.notegather.admin.domain.user.model.User;
import com.notegather.admin.domain.user.repository.UserRepository;
import com.notegather.admin.infrastructure.persistence.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MybatisUserRepository implements UserRepository {

    private final UserMapper userMapper;

    @Override
    public User findById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("LIMIT 1"));
    }

    @Override
    public List<User> findByIds(List<Long> userIds) {
        return userMapper.selectBatchIds(userIds);
    }

    @Override
    public void save(User user) {
        userMapper.insert(user);
    }

    @Override
    public boolean updateStatus(Long userId, Integer status) {
        User user = new User();
        user.setId(userId);
        user.setStatus(status);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean updateAvatarUrl(Long userId, String avatarUrl) {
        User user = new User();
        user.setId(userId);
        user.setAvatarUrl(avatarUrl);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean updateProfile(Long userId, String nickname, String bio) {
        User user = new User();
        user.setId(userId);
        user.setNickname(nickname);
        user.setBio(bio);
        return userMapper.updateById(user) > 0;
    }
}
