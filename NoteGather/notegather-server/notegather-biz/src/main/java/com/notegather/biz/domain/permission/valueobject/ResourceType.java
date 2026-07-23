package com.notegather.biz.domain.permission.valueobject;

import lombok.Getter;

/**
 * 资源类型枚举
 */
@Getter
public enum ResourceType {
    
    /**
     * 知识库
     */
    KNOWLEDGE_BASE("knowledge_base", "知识库"),
    
    /**
     * 文档
     */
    DOCUMENT("document", "文档");
    
    private final String code;
    private final String displayName;
    
    ResourceType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    /**
     * 从代码转换为枚举
     */
    public static ResourceType fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("资源类型代码不能为空");
        }
        for (ResourceType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的资源类型: " + code);
    }
}
