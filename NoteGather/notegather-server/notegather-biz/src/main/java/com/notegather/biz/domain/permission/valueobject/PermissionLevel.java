package com.notegather.biz.domain.permission.valueobject;

import lombok.Getter;

/**
 * 权限级别枚举
 * Owner > Editor > Companion > Viewer
 */
@Getter
public enum PermissionLevel {
    
    /**
     * 所有者：完全控制权限，包括删除资源和管理成员
     */
    OWNER("owner", "所有者", 40),
    
    /**
     * 编辑者：可以编辑内容，但不能删除资源或管理成员
     */
    EDITOR("editor", "编辑者", 30),
    
    /**
     * 协作者：可以评论和查看，但不能编辑
     */
    COMPANION("companion", "协作者", 20),
    
    /**
     * 查看者：只读权限
     */
    VIEWER("viewer", "查看者", 10);
    
    private final String code;
    private final String displayName;
    private final int level;
    
    PermissionLevel(String code, String displayName, int level) {
        this.code = code;
        this.displayName = displayName;
        this.level = level;
    }
    
    /**
     * 从代码转换为枚举
     */
    public static PermissionLevel fromCode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("权限级别代码不能为空");
        }
        for (PermissionLevel level : values()) {
            if (level.code.equalsIgnoreCase(code)) {
                return level;
            }
        }
        throw new IllegalArgumentException("未知的权限级别: " + code);
    }
    
    /**
     * 检查当前权限是否高于或等于目标权限
     */
    public boolean isAtLeast(PermissionLevel target) {
        return this.level >= target.level;
    }
    
    /**
     * 检查当前权限是否高于目标权限
     */
    public boolean isHigherThan(PermissionLevel target) {
        return this.level > target.level;
    }
    
    /**
     * 检查是否为 Owner
     */
    public boolean isOwner() {
        return this == OWNER;
    }
    
    /**
     * 检查是否可以编辑（Owner 或 Editor）
     */
    public boolean canEdit() {
        return this.level >= EDITOR.level;
    }
    
    /**
     * 检查是否可以查看（所有级别都可以）
     */
    public boolean canView() {
        return true;
    }
}
