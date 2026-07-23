package com.notegather.biz.domain.knowledge.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 收藏ID值对象
 */
@Getter
@EqualsAndHashCode
public class FavoriteId {
    
    private final Long value;
    
    private FavoriteId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("收藏ID不能为空或小于等于0");
        }
        this.value = value;
    }
    
    public static FavoriteId of(Long value) {
        return new FavoriteId(value);
    }
    
    @Override
    public String toString() {
        return "FavoriteId(" + value + ")";
    }
}
