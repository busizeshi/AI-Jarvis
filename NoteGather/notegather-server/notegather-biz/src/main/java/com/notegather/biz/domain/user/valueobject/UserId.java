package com.notegather.biz.domain.user.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 用户ID值对象
 */
@Getter
@EqualsAndHashCode
public class UserId {
    
    private final Long value;
    
    private UserId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("用户ID不能为空或小于等于0");
        }
        this.value = value;
    }
    
    /**
     * 创建用户ID
     */
    public static UserId of(Long value) {
        return new UserId(value);
    }
    
    /**
     * 从数据库ID重建
     */
    public static UserId reconstruct(Long id) {
        if (id == null) {
            return null;
        }
        return new UserId(id);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
