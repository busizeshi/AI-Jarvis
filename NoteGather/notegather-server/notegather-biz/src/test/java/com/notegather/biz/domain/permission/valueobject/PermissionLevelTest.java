package com.notegather.biz.domain.permission.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PermissionLevel 权限级别单元测试
 */
@DisplayName("权限级别测试")
class PermissionLevelTest {
    
    @Test
    @DisplayName("从代码转换 - Owner")
    void fromCode_Owner_Success() {
        // When
        PermissionLevel level = PermissionLevel.fromCode("owner");
        
        // Then
        assertThat(level).isEqualTo(PermissionLevel.OWNER);
        assertThat(level.getDisplayName()).isEqualTo("所有者");
        assertThat(level.getLevel()).isEqualTo(40);
    }
    
    @Test
    @DisplayName("从代码转换 - 大小写不敏感")
    void fromCode_CaseInsensitive_Success() {
        // When
        PermissionLevel level1 = PermissionLevel.fromCode("OWNER");
        PermissionLevel level2 = PermissionLevel.fromCode("Owner");
        PermissionLevel level3 = PermissionLevel.fromCode("owner");
        
        // Then
        assertThat(level1).isEqualTo(PermissionLevel.OWNER);
        assertThat(level2).isEqualTo(PermissionLevel.OWNER);
        assertThat(level3).isEqualTo(PermissionLevel.OWNER);
    }
    
    @Test
    @DisplayName("从代码转换 - 无效代码抛出异常")
    void fromCode_InvalidCode_ThrowException() {
        // When & Then
        assertThatThrownBy(() -> PermissionLevel.fromCode("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("未知的权限级别");
    }
    
    @Test
    @DisplayName("从代码转换 - 空代码抛出异常")
    void fromCode_NullCode_ThrowException() {
        // When & Then
        assertThatThrownBy(() -> PermissionLevel.fromCode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("权限级别代码不能为空");
    }
    
    @Test
    @DisplayName("权限级别比较 - isAtLeast")
    void isAtLeast_VariousCases() {
        // Owner >= Owner
        assertThat(PermissionLevel.OWNER.isAtLeast(PermissionLevel.OWNER)).isTrue();
        
        // Owner >= Editor
        assertThat(PermissionLevel.OWNER.isAtLeast(PermissionLevel.EDITOR)).isTrue();
        
        // Editor >= Viewer
        assertThat(PermissionLevel.EDITOR.isAtLeast(PermissionLevel.VIEWER)).isTrue();
        
        // Viewer < Editor
        assertThat(PermissionLevel.VIEWER.isAtLeast(PermissionLevel.EDITOR)).isFalse();
        
        // Companion >= Viewer
        assertThat(PermissionLevel.COMPANION.isAtLeast(PermissionLevel.VIEWER)).isTrue();
    }
    
    @Test
    @DisplayName("权限级别比较 - isHigherThan")
    void isHigherThan_VariousCases() {
        // Owner > Editor
        assertThat(PermissionLevel.OWNER.isHigherThan(PermissionLevel.EDITOR)).isTrue();
        
        // Editor > Companion
        assertThat(PermissionLevel.EDITOR.isHigherThan(PermissionLevel.COMPANION)).isTrue();
        
        // Companion > Viewer
        assertThat(PermissionLevel.COMPANION.isHigherThan(PermissionLevel.VIEWER)).isTrue();
        
        // Owner == Owner (not higher)
        assertThat(PermissionLevel.OWNER.isHigherThan(PermissionLevel.OWNER)).isFalse();
        
        // Viewer < Editor (not higher)
        assertThat(PermissionLevel.VIEWER.isHigherThan(PermissionLevel.EDITOR)).isFalse();
    }
    
    @Test
    @DisplayName("检查是否为 Owner")
    void isOwner_VariousCases() {
        assertThat(PermissionLevel.OWNER.isOwner()).isTrue();
        assertThat(PermissionLevel.EDITOR.isOwner()).isFalse();
        assertThat(PermissionLevel.COMPANION.isOwner()).isFalse();
        assertThat(PermissionLevel.VIEWER.isOwner()).isFalse();
    }
    
    @Test
    @DisplayName("检查是否可以编辑")
    void canEdit_VariousCases() {
        assertThat(PermissionLevel.OWNER.canEdit()).isTrue();
        assertThat(PermissionLevel.EDITOR.canEdit()).isTrue();
        assertThat(PermissionLevel.COMPANION.canEdit()).isFalse();
        assertThat(PermissionLevel.VIEWER.canEdit()).isFalse();
    }
    
    @Test
    @DisplayName("检查是否可以查看")
    void canView_AllLevels_ReturnTrue() {
        assertThat(PermissionLevel.OWNER.canView()).isTrue();
        assertThat(PermissionLevel.EDITOR.canView()).isTrue();
        assertThat(PermissionLevel.COMPANION.canView()).isTrue();
        assertThat(PermissionLevel.VIEWER.canView()).isTrue();
    }
    
    @Test
    @DisplayName("权限级别排序")
    void permissionLevelOrdering() {
        assertThat(PermissionLevel.OWNER.getLevel())
                .isGreaterThan(PermissionLevel.EDITOR.getLevel());
        
        assertThat(PermissionLevel.EDITOR.getLevel())
                .isGreaterThan(PermissionLevel.COMPANION.getLevel());
        
        assertThat(PermissionLevel.COMPANION.getLevel())
                .isGreaterThan(PermissionLevel.VIEWER.getLevel());
    }
}
