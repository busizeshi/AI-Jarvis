package com.notegather.biz.domain.knowledge.valueobject;

/**
 * 文档类型枚举
 */
public enum DocumentType {
    
    /**
     * 文件夹
     */
    FOLDER(0, "文件夹"),
    
    /**
     * 文档
     */
    DOCUMENT(1, "文档");
    
    private final int code;
    private final String description;
    
    DocumentType(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static DocumentType fromCode(int code) {
        for (DocumentType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的文档类型代码: " + code);
    }
}
