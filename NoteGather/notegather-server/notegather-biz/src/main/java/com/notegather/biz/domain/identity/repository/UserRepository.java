package com.notegather.biz.domain.identity.repository;

import com.notegather.biz.domain.identity.aggregate.User;
import com.notegather.biz.domain.identity.valueobject.Email;
import com.notegather.biz.domain.identity.valueobject.UserId;

import java.util.Optional;

/**
 * 用户仓储接口（领域层）
 */
public interface UserRepository {
    
    /**
     * 保存用户（新增或更新）
     */
    void save(User user);
    
    /**
     * 根据ID查询用户
     */
    Optional<User> findById(UserId userId);
    
    /**
     * 根据用户名查询用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查询用户
     */
    Optional<User> findByEmail(Email email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(Email email);
    
    /**
     * 删除用户（物理删除，谨慎使用）
     */
    void delete(UserId userId);
}
