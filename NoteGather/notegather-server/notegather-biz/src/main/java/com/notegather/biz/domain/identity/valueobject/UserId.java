package com.notegather.biz.domain.identity.valueobject;

import java.io.Serializable;
import java.util.Objects;

/**
 * 用户ID值对象
 */
public class UserId implements Serializable {
    
    private final Long value;
    
    private UserId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("用户ID不能为空或小于等于0");
        }
        this.value = value;
    }
    
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
    
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
