package com.notegather.admin.domain.user.repository;

import com.notegather.admin.domain.user.model.User;

import java.util.List;

public interface UserRepository {

    User findById(Long userId);

    User findByUsername(String username);

    List<User> findByIds(List<Long> userIds);

    void save(User user);

    boolean updateStatus(Long userId, Integer status);
}
