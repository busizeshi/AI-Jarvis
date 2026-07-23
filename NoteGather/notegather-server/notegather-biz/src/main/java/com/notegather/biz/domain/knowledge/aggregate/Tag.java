package com.notegather.biz.domain.knowledge.aggregate;

import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.valueobject.TagId;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 标签聚合根
 * 
 * 职责：
 * 1. 管理标签的基本信息（名称、颜色）
 * 2. 标签的所有权验证
 * 3. 标签的创建和更新逻辑
 */
@Getter
public class Tag {
    
    private TagId id;
    private UserId ownerId;
    private String name;
    private String color;  // 十六进制颜色值，如 #FF5733
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;
    
    /**
     * 创建新标签（工厂方法）
     */
    public static Tag create(UserId ownerId, String name, String color) {
        Tag tag = new Tag();
        tag.ownerId = ownerId;
        tag.name = validateName(name);
        tag.color = validateColor(color);
        tag.createdAt = LocalDateTime.now();
        tag.updatedAt = LocalDateTime.now();
        tag.deleted = false;
        return tag;
    }
    
    /**
     * 从数据库重建（重建模式）
     */
    public static Tag reconstitute(Long id, Long ownerId, String name, String color,
                                   LocalDateTime createdAt, LocalDateTime updatedAt, boolean deleted) {
        Tag tag = new Tag();
        tag.id = TagId.of(id);
        tag.ownerId = UserId.of(ownerId);
        tag.name = name;
        tag.color = color;
        tag.createdAt = createdAt;
        tag.updatedAt = updatedAt;
        tag.deleted = deleted;
        return tag;
    }
    
    /**
     * 更新标签信息
     */
    public void update(String name, String color) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = validateName(name);
        }
        if (color != null && !color.trim().isEmpty()) {
            this.color = validateColor(color);
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 软删除
     */
    public void markAsDeleted() {
        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 检查是否为所有者
     */
    public boolean isOwnedBy(UserId userId) {
        return this.ownerId.equals(userId);
    }
    
    /**
     * 设置ID（保存后回填）
     */
    public void setId(Long id) {
        this.id = TagId.of(id);
    }
    
    /**
     * 验证标签名称
     */
    private static String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("标签名称不能为空");
        }
        if (name.length() > 32) {
            throw new IllegalArgumentException("标签名称不能超过32个字符");
        }
        return name.trim();
    }
    
    /**
     * 验证颜色值
     */
    private static String validateColor(String color) {
        if (color == null || color.trim().isEmpty()) {
            return "#666666";  // 默认灰色
        }
        String trimmedColor = color.trim();
        // 简单的十六进制颜色校验
        if (!trimmedColor.matches("^#[0-9A-Fa-f]{6}$")) {
            throw new IllegalArgumentException("颜色格式不正确，应为 #RRGGBB 格式");
        }
        return trimmedColor;
    }
}
