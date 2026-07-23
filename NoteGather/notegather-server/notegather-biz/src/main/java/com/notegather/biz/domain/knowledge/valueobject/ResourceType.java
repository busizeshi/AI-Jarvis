package com.notegather.biz.domain.knowledge.valueobject;

import lombok.Getter;

/**
 * 资源类型枚举
 */
@Getter
public enum ResourceType {
    
    DOCUMENT("document", "文档"),
    KNOWLEDGE_BASE("knowledge_base", "知识库");
    
    private final String code;
    private final String description;
    
    ResourceType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public static ResourceType fromCode(String code) {
        for (ResourceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的资源类型: " + code);
    }
}
