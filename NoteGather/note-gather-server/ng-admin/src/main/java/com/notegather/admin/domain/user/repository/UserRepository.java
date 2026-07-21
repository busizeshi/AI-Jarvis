package com.notegather.admin.domain.user.repository;

import com.notegather.admin.domain.user.model.User;

import java.util.List;

public interface UserRepository {

    User findById(Long userId);

    User findByUsername(String username);

    List<User> findByIds(List<Long> userIds);

    void save(User user);

    boolean updateStatus(Long userId, Integer status);

    /** 更新用户头像 URL（置空时传 null） */
    boolean updateAvatarUrl(Long userId, String avatarUrl);

    /** 更新用户基本资料（昵称、简介） */
    boolean updateProfile(Long userId, String nickname, String bio);
}
