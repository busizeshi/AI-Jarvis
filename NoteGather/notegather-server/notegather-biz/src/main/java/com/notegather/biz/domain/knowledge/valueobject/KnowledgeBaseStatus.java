package com.notegather.biz.domain.knowledge.valueobject;

/**
 * 知识库状态枚举
 * 
 * @author NoteGather
 * @since 1.0.0
 */
public enum KnowledgeBaseStatus {
    
    /**
     * 禁用
     */
    DISABLED(0, "禁用"),
    
    /**
     * 启用
     */
    ENABLED(1, "启用");
    
    private final int code;
    private final String description;
    
    KnowledgeBaseStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static KnowledgeBaseStatus fromCode(int code) {
        for (KnowledgeBaseStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的知识库状态代码: " + code);
    }
}
