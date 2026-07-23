package com.notegather.biz.domain.knowledge.aggregate;

import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.valueobject.TagId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tag 聚合根单元测试
 */
@DisplayName("标签聚合根测试")
class TagTest {
    
    @Test
    @DisplayName("创建标签 - 成功")
    void createTag_Success() {
        UserId ownerId = UserId.of(1L);
        String name = "Java";
        String color = "#FF0000";
        
        Tag tag = Tag.create(ownerId, name, color);
        
        assertThat(tag).isNotNull();
        assertThat(tag.getName()).isEqualTo(name);
        assertThat(tag.getColor()).isEqualTo(color);
        assertThat(tag.getOwnerId()).isEqualTo(ownerId);
        assertThat(tag.getCreatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("创建标签 - 名称为空抛出异常")
    void createTag_NullName_ThrowException() {
        UserId ownerId = UserId.of(1L);
        
        assertThatThrownBy(() -> Tag.create(ownerId, null, "#FF0000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("标签名称不能为空");
    }
    
    @Test
    @DisplayName("检查标签所有权 - 是所有者")
    void isOwnedBy_IsOwner_ReturnTrue() {
        UserId ownerId = UserId.of(1L);
        Tag tag = Tag.create(ownerId, "Java", "#FF0000");
        
        boolean result = tag.isOwnedBy(ownerId);
        
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("检查标签所有权 - 不是所有者")
    void isOwnedBy_NotOwner_ReturnFalse() {
        UserId ownerId = UserId.of(1L);
        UserId otherUserId = UserId.of(2L);
        Tag tag = Tag.create(ownerId, "Java", "#FF0000");
        
        boolean result = tag.isOwnedBy(otherUserId);
        
        assertThat(result).isFalse();
    }
}
