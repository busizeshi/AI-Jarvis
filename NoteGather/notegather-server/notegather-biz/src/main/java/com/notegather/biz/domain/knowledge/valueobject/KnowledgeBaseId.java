package com.notegather.biz.domain.knowledge.valueobject;

import java.util.Objects;

/**
 * 知识库ID值对象
 */
public class KnowledgeBaseId {
    
    private final Long value;
    
    private KnowledgeBaseId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("知识库ID不能为空或小于等于0");
        }
        this.value = value;
    }
    
    public static KnowledgeBaseId of(Long value) {
        return new KnowledgeBaseId(value);
    }
    
    public Long getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeBaseId that = (KnowledgeBaseId) o;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "KnowledgeBaseId{" + value + "}";
    }
}
