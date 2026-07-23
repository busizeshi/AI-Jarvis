package com.notegather.biz.domain.knowledge.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 标签ID值对象
 */
@Getter
@EqualsAndHashCode
public class TagId {
    
    private final Long value;
    
    private TagId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("标签ID不能为空或小于等于0");
        }
        this.value = value;
    }
    
    public static TagId of(Long value) {
        return new TagId(value);
    }
    
    @Override
    public String toString() {
        return "TagId(" + value + ")";
    }
}
