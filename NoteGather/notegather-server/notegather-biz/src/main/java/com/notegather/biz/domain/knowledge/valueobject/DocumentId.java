package com.notegather.biz.domain.knowledge.valueobject;

import java.util.Objects;

/**
 * 文档ID值对象
 */
public class DocumentId {
    
    private final Long value;
    
    private DocumentId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("文档ID不能为空或小于等于0");
        }
        this.value = value;
    }
    
    public static DocumentId of(Long value) {
        return new DocumentId(value);
    }
    
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentId that = (DocumentId) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "DocumentId{" + value + "}";
    }
}
