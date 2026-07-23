package com.notegather.biz.domain.knowledge.valueobject;

/**
 * 可见性枚举
 * 
 * @author NoteGather
 * @since 1.0.0
 */
public enum Visibility {
    
    /**
     * 私有 - 仅所有者可见
     */
    PRIVATE(0, "私有"),
    
    /**
     * 团队 - 团队成员可见
     */
    TEAM(1, "团队"),
    
    /**
     * 公开 - 所有人可见
     */
    PUBLIC(2, "公开");
    
    private final int code;
    private final String description;
    
    Visibility(int code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static Visibility fromCode(int code) {
        for (Visibility visibility : values()) {
            if (visibility.code == code) {
                return visibility;
            }
        }
        throw new IllegalArgumentException("未知的可见性代码: " + code);
    }
}
