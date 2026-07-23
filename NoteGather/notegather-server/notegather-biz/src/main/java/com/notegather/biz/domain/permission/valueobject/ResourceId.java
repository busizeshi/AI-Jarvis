package com.notegather.biz.domain.permission.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 资源ID值对象
 * 用于表示知识库或文档的ID
 */
@Getter
@EqualsAndHashCode
public class ResourceId {
    
    private final Long value;
    
    private ResourceId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("资源ID不能为空或小于等于0");
        }
        this.value = value;
    }
    
    /**
     * 创建资源ID
     */
    public static ResourceId of(Long value) {
        return new ResourceId(value);
    }
    
    /**
     * 从数据库ID重建
     */
    public static ResourceId reconstruct(Long id) {
        if (id == null) {
            return null;
        }
        return new ResourceId(id);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
