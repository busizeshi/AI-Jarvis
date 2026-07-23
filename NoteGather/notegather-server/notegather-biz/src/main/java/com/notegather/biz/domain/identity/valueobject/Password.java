package com.notegather.biz.domain.identity.valueobject;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.Serializable;
import java.util.Objects;

/**
 * 密码值对象
 * 封装密码加密和校验逻辑
 */
public class Password implements Serializable {
    
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();
    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 32;
    
    private final String hash;
    
    private Password(String hash) {
        this.hash = hash;
    }
    
    /**
     * 从明文密码创建（用于注册）
     */
    public static Password fromRaw(String rawPassword) {
        validateRawPassword(rawPassword);
        String hash = PASSWORD_ENCODER.encode(rawPassword);
        return new Password(hash);
    }
    
    /**
     * 从密码哈希创建（用于从数据库加载）
     */
    public static Password fromHash(String hash) {
        if (hash == null || hash.trim().isEmpty()) {
            throw new IllegalArgumentException("密码哈希不能为空");
        }
        return new Password(hash);
    }
    
    /**
     * 验证明文密码是否匹配
     */
    public boolean matches(String rawPassword) {
        return PASSWORD_ENCODER.matches(rawPassword, this.hash);
    }
    
    /**
     * 获取密码哈希（用于持久化）
     */
    public String getHash() {
        return hash;
    }
    
    private static void validateRawPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (rawPassword.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("密码长度不能少于" + MIN_LENGTH + "位");
        }
        if (rawPassword.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("密码长度不能超过" + MAX_LENGTH + "位");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(hash, password.hash);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(hash);
    }
}
