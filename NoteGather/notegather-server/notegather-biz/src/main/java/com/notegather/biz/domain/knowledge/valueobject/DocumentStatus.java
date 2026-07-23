package com.notegather.biz.domain.knowledge.valueobject;

/**
 * 文档状态枚举
 */
public enum DocumentStatus {
    
    /**
     * 草稿
     */
    DRAFT(0, "草稿"),
    
    /**
     * 已发布
     */
    PUBLISHED(1, "已发布");
    
    private final int code;
    private final String description;
    
    DocumentStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static DocumentStatus fromCode(int code) {
        for (DocumentStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的文档状态代码: " + code);
    }
}
