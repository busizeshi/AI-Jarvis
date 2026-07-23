package com.notegather.biz.application.service;

import cn.dev33.satoken.stp.StpUtil;
import com.notegather.biz.application.command.RegisterCommand;
import com.notegather.biz.application.command.LoginCommand;
import com.notegather.biz.domain.identity.aggregate.User;
import com.notegather.biz.domain.identity.repository.UserRepository;
import com.notegather.biz.domain.identity.valueobject.Email;
import com.notegather.common.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证应用服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    
    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterCommand command) {
        // 检查用户名是否存在
        if (userRepository.existsByUsername(command.getUsername())) {
            throw new BusinessException("CONFLICT", "用户名已存在");
        }
        
        // 检查邮箱是否存在
        Email email = Email.of(command.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("CONFLICT", "邮箱已被注册");
        }
        
        // 创建用户
        User user = User.create(
            command.getUsername(),
            command.getEmail(),
            command.getPassword(),
            command.getDisplayName()
        );
        
        // 保存用户
        userRepository.save(user);
        
        return user.getId().getValue();
    }
    
    /**
     * 用户登录
     */
    public String login(LoginCommand command) {
        // 查询用户
        User user = userRepository.findByUsername(command.getUsername())
            .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        
        // 检查用户状态
        user.ensureActive();
        
        // 验证密码
        if (!user.verifyPassword(command.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        
        // Sa-Token 登录
        StpUtil.login(user.getId().getValue());
        
        // 返回 Token
        return StpUtil.getTokenValue();
    }
    
    /**
     * 退出登录
     */
    public void logout() {
        StpUtil.logout();
    }
    
    /**
     * 刷新 Token（续期）
     */
    public void refreshToken() {
        StpUtil.checkLogin();
        // Sa-Token 会自动续期，无需额外操作
    }
    
    /**
     * 获取当前登录用户ID
     */
    public Long getCurrentUserId() {
        return StpUtil.getLoginIdAsLong();
    }
    
    /**
     * 检查是否已登录
     */
    public boolean isLogin() {
        return StpUtil.isLogin();
    }
}
