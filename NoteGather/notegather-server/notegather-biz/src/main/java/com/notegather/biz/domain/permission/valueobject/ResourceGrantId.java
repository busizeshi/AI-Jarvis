package com.notegather.biz.domain.permission.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 资源授权ID值对象
 */
@Getter
@EqualsAndHashCode
public class ResourceGrantId {
    
    private final Long value;
    
    private ResourceGrantId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("资源授权ID不能为空或小于等于0");
        }
        this.value = value;
    }
    
    /**
     * 创建资源授权ID
     */
    public static ResourceGrantId of(Long value) {
        return new ResourceGrantId(value);
    }
    
    /**
     * 从数据库ID重建
     */
    public static ResourceGrantId reconstruct(Long id) {
        if (id == null) {
            return null;
        }
        return new ResourceGrantId(id);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
