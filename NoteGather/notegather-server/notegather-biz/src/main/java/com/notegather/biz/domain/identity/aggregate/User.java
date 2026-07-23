package com.notegather.biz.domain.identity.aggregate;

import com.notegather.biz.domain.identity.valueobject.Email;
import com.notegather.biz.domain.identity.valueobject.Password;
import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.identity.valueobject.UserStatus;

import java.time.LocalDateTime;

/**
 * 用户聚合根
 */
public class User {
    
    private UserId id;
    private String username;
    private Email email;
    private Password password;
    private String displayName;
    private String avatarUrl;
    private UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;
    
    /**
     * 创建新用户（注册）
     */
    public static User create(String username, String email, String rawPassword, String displayName) {
        User user = new User();
        user.username = validateUsername(username);
        user.email = Email.of(email);
        user.password = Password.fromRaw(rawPassword);
        user.displayName = displayName;
        user.status = UserStatus.ENABLED;
        user.deleted = false;
        user.createdAt = LocalDateTime.now();
        user.updatedAt = LocalDateTime.now();
        return user;
    }
    
    /**
     * 从数据库重建（DDD 重建模式）
     */
    public static User reconstitute(Long id, String username, String email, String passwordHash,
                                    String displayName, String avatarUrl, int statusCode,
                                    LocalDateTime createdAt, LocalDateTime updatedAt, boolean deleted) {
        User user = new User();
        user.id = UserId.of(id);
        user.username = username;
        user.email = Email.of(email);
        user.password = Password.fromHash(passwordHash);
        user.displayName = displayName;
        user.avatarUrl = avatarUrl;
        user.status = UserStatus.fromCode(statusCode);
        user.createdAt = createdAt;
        user.updatedAt = updatedAt;
        user.deleted = deleted;
        return user;
    }
    
    /**
     * 验证密码
     */
    public boolean verifyPassword(String rawPassword) {
        return password.matches(rawPassword);
    }
    
    /**
     * 更新密码
     */
    public void updatePassword(String oldPassword, String newPassword) {
        if (!verifyPassword(oldPassword)) {
            throw new IllegalArgumentException("原密码不正确");
        }
        this.password = Password.fromRaw(newPassword);
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 更新资料
     */
    public void updateProfile(String displayName, String avatarUrl) {
        if (displayName != null && !displayName.trim().isEmpty()) {
            this.displayName = displayName;
        }
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            this.avatarUrl = avatarUrl;
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 启用用户
     */
    public void enable() {
        this.status = UserStatus.ENABLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 禁用用户
     */
    public void disable() {
        this.status = UserStatus.DISABLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查用户是否可用
     */
    public void ensureActive() {
        if (deleted) {
            throw new IllegalStateException("用户已被删除");
        }
        if (!status.isEnabled()) {
            throw new IllegalStateException("用户已被禁用");
        }
    }
    
    private static String validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (username.length() < 3 || username.length() > 32) {
            throw new IllegalArgumentException("用户名长度必须在3-32位之间");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("用户名只能包含字母、数字和下划线");
        }
        return username;
    }
    
    // Getters
    public UserId getId() {
        return id;
    }
    
    /**
     * 设置 ID（仅供仓储层回填使用）
     */
    public void setId(Long id) {
        this.id = UserId.of(id);
    }
    
    public String getUsername() {
        return username;
    }
    
    public Email getEmail() {
        return email;
    }
    
    public Password getPassword() {
        return password;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public boolean isDeleted() {
        return deleted;
    }
}
