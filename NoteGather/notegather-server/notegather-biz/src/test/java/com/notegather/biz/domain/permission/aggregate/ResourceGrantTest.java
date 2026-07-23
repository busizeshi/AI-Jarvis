package com.notegather.biz.domain.permission.aggregate;

import com.notegather.biz.domain.permission.valueobject.*;
import com.notegather.biz.domain.identity.valueobject.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ResourceGrant 聚合根单元测试
 */
@DisplayName("资源授权聚合根测试")
class ResourceGrantTest {
    
    @Test
    @DisplayName("创建显式授权 - 成功")
    void createGrant_Success() {
        ResourceType resourceType = ResourceType.KNOWLEDGE_BASE;
        ResourceId resourceId = ResourceId.of(1L);
        UserId grantedUserId = UserId.of(2L);
        PermissionLevel level = PermissionLevel.EDITOR;
        UserId grantedByUserId = UserId.of(1L);
        
        ResourceGrant grant = ResourceGrant.create(
                resourceType, resourceId, grantedUserId, level, grantedByUserId);
        
        assertThat(grant).isNotNull();
        assertThat(grant.getResourceType()).isEqualTo(resourceType);
        assertThat(grant.getResourceId()).isEqualTo(resourceId);
        assertThat(grant.getGrantedUserId()).isEqualTo(grantedUserId);
        assertThat(grant.getPermissionLevel()).isEqualTo(level);
        assertThat(grant.getCreatedAt()).isNotNull();
    }
    
    @Test
    @DisplayName("检查权限 - 有足够权限")
    void hasPermission_SufficientPermission_ReturnTrue() {
        ResourceGrant grant = ResourceGrant.create(
                ResourceType.KNOWLEDGE_BASE,
                ResourceId.of(1L),
                UserId.of(2L),
                PermissionLevel.EDITOR,
                UserId.of(1L));
        
        assertThat(grant.hasPermission(PermissionLevel.VIEWER)).isTrue();
        assertThat(grant.hasPermission(PermissionLevel.EDITOR)).isTrue();
    }
    
    @Test
    @DisplayName("检查权限 - 权限不足")
    void hasPermission_InsufficientPermission_ReturnFalse() {
        ResourceGrant grant = ResourceGrant.create(
                ResourceType.KNOWLEDGE_BASE,
                ResourceId.of(1L),
                UserId.of(2L),
                PermissionLevel.VIEWER,
                UserId.of(1L));
        
        assertThat(grant.hasPermission(PermissionLevel.EDITOR)).isFalse();
        assertThat(grant.hasPermission(PermissionLevel.OWNER)).isFalse();
    }
}
