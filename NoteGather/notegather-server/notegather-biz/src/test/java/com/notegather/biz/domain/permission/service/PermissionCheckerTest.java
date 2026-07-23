package com.notegather.biz.domain.permission.service;

import com.notegather.biz.domain.permission.aggregate.ResourceGrant;
import com.notegather.biz.domain.permission.repository.ResourceGrantRepository;
import com.notegather.biz.domain.permission.valueobject.PermissionLevel;
import com.notegather.biz.domain.permission.valueobject.ResourceId;
import com.notegather.biz.domain.permission.valueobject.ResourceType;
import com.notegather.biz.domain.identity.valueobject.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PermissionChecker 领域服务测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("权限检查器测试")
class PermissionCheckerTest {
    
    @Mock
    private ResourceGrantRepository resourceGrantRepository;
    
    @InjectMocks
    private PermissionChecker permissionChecker;
    
    private UserId userId;
    private ResourceType resourceType;
    private ResourceId resourceId;
    private ResourceGrant ownerGrant;
    private ResourceGrant editorGrant;
    private ResourceGrant viewerGrant;
    
    @BeforeEach
    void setUp() {
        userId = UserId.of(1L);
        resourceType = ResourceType.KNOWLEDGE_BASE;
        resourceId = ResourceId.of(100L);
        
        // 创建不同权限级别的授权
        ownerGrant = ResourceGrant.create(
                resourceType, resourceId, userId, PermissionLevel.OWNER, UserId.of(1L));
        editorGrant = ResourceGrant.create(
                resourceType, resourceId, userId, PermissionLevel.EDITOR, UserId.of(1L));
        viewerGrant = ResourceGrant.create(
                resourceType, resourceId, userId, PermissionLevel.VIEWER, UserId.of(1L));
    }
    
    @Test
    @DisplayName("检查权限 - 用户有足够权限")
    void hasPermission_UserHasSufficientPermission_ReturnTrue() {
        when(resourceGrantRepository.findByUserIdAndResource(userId, resourceType, resourceId))
                .thenReturn(Optional.of(editorGrant));
        
        boolean result = permissionChecker.hasPermission(
                userId, resourceType, resourceId, PermissionLevel.VIEWER);
        
        assertThat(result).isTrue();
        verify(resourceGrantRepository).findByUserIdAndResource(userId, resourceType, resourceId);
    }
    
    @Test
    @DisplayName("检查权限 - 用户权限不足")
    void hasPermission_UserHasInsufficientPermission_ReturnFalse() {
        when(resourceGrantRepository.findByUserIdAndResource(userId, resourceType, resourceId))
                .thenReturn(Optional.of(viewerGrant));
        
        boolean result = permissionChecker.hasPermission(
                userId, resourceType, resourceId, PermissionLevel.EDITOR);
        
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("检查权限 - 用户无任何授权")
    void hasPermission_UserHasNoGrant_ReturnFalse() {
        when(resourceGrantRepository.findByUserIdAndResource(any(), any(), any()))
                .thenReturn(Optional.empty());
        
        boolean result = permissionChecker.hasPermission(
                userId, resourceType, resourceId, PermissionLevel.VIEWER);
        
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("检查权限 - 参数为null返回false")
    void hasPermission_NullParameters_ReturnFalse() {
        assertThat(permissionChecker.hasPermission(null, resourceType, resourceId, PermissionLevel.VIEWER))
                .isFalse();
        assertThat(permissionChecker.hasPermission(userId, null, resourceId, PermissionLevel.VIEWER))
                .isFalse();
        assertThat(permissionChecker.hasPermission(userId, resourceType, null, PermissionLevel.VIEWER))
                .isFalse();
        assertThat(permissionChecker.hasPermission(userId, resourceType, resourceId, null))
                .isFalse();
        
        verifyNoInteractions(resourceGrantRepository);
    }
    
    @Test
    @DisplayName("检查是否为Owner - 是Owner")
    void isOwner_UserIsOwner_ReturnTrue() {
        when(resourceGrantRepository.findByUserIdAndResource(userId, resourceType, resourceId))
                .thenReturn(Optional.of(ownerGrant));
        
        boolean result = permissionChecker.isOwner(userId, resourceType, resourceId);
        
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("检查是否为Owner - 不是Owner")
    void isOwner_UserIsNotOwner_ReturnFalse() {
        when(resourceGrantRepository.findByUserIdAndResource(userId, resourceType, resourceId))
                .thenReturn(Optional.of(editorGrant));
        
        boolean result = permissionChecker.isOwner(userId, resourceType, resourceId);
        
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("检查是否可以编辑 - 可以编辑")
    void canEdit_UserCanEdit_ReturnTrue() {
        when(resourceGrantRepository.findByUserIdAndResource(userId, resourceType, resourceId))
                .thenReturn(Optional.of(editorGrant));
        
        boolean result = permissionChecker.canEdit(userId, resourceType, resourceId);
        
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("检查是否可以编辑 - 不可以编辑")
    void canEdit_UserCannotEdit_ReturnFalse() {
        when(resourceGrantRepository.findByUserIdAndResource(userId, resourceType, resourceId))
                .thenReturn(Optional.of(viewerGrant));
        
        boolean result = permissionChecker.canEdit(userId, resourceType, resourceId);
        
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("检查是否可以查看 - 可以查看")
    void canView_UserCanView_ReturnTrue() {
        when(resourceGrantRepository.findByUserIdAndResource(userId, resourceType, resourceId))
                .thenReturn(Optional.of(viewerGrant));
        
        boolean result = permissionChecker.canView(userId, resourceType, resourceId);
        
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("获取权限级别 - 有权限")
    void getPermissionLevel_UserHasPermission_ReturnLevel() {
        when(resourceGrantRepository.findByUserIdAndResource(userId, resourceType, resourceId))
                .thenReturn(Optional.of(editorGrant));
        
        Optional<PermissionLevel> result = permissionChecker.getPermissionLevel(
                userId, resourceType, resourceId);
        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(PermissionLevel.EDITOR);
    }
    
    @Test
    @DisplayName("获取权限级别 - 无权限")
    void getPermissionLevel_UserHasNoPermission_ReturnEmpty() {
        when(resourceGrantRepository.findByUserIdAndResource(userId, resourceType, resourceId))
                .thenReturn(Optional.empty());
        
        Optional<PermissionLevel> result = permissionChecker.getPermissionLevel(
                userId, resourceType, resourceId);
        
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("检查是否可以授权 - Owner可以授权")
    void canGrant_UserIsOwner_ReturnTrue() {
        when(resourceGrantRepository.findByUserIdAndResource(userId, resourceType, resourceId))
                .thenReturn(Optional.of(ownerGrant));
        
        boolean result = permissionChecker.canGrant(
                userId, resourceType, resourceId, PermissionLevel.EDITOR);
        
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("检查是否可以授权 - 非Owner不可以授权")
    void canGrant_UserIsNotOwner_ReturnFalse() {
        when(resourceGrantRepository.findByUserIdAndResource(userId, resourceType, resourceId))
                .thenReturn(Optional.of(editorGrant));
        
        boolean result = permissionChecker.canGrant(
                userId, resourceType, resourceId, PermissionLevel.VIEWER);
        
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("检查是否可以撤销 - Owner可以撤销他人权限")
    void canRevoke_OwnerRevokingOthers_ReturnTrue() {
        UserId ownerId = UserId.of(1L);
        UserId otherId = UserId.of(2L);
        
        ResourceGrant ownerGrantForOwner = ResourceGrant.create(
                resourceType, resourceId, ownerId, PermissionLevel.OWNER, ownerId);
        ResourceGrant grantToRevoke = ResourceGrant.create(
                resourceType, resourceId, otherId, PermissionLevel.EDITOR, ownerId);
        
        when(resourceGrantRepository.findByUserIdAndResource(ownerId, resourceType, resourceId))
                .thenReturn(Optional.of(ownerGrantForOwner));
        
        boolean result = permissionChecker.canRevoke(
                ownerId, resourceType, resourceId, grantToRevoke);
        
        assertThat(result).isTrue();
    }
    
    @Test
    @DisplayName("检查是否可以撤销 - Owner不可以撤销自己的Owner权限")
    void canRevoke_OwnerRevokingOwnOwnerPermission_ReturnFalse() {
        UserId ownerId = UserId.of(1L);
        
        ResourceGrant ownerGrantForOwner = ResourceGrant.create(
                resourceType, resourceId, ownerId, PermissionLevel.OWNER, ownerId);
        
        when(resourceGrantRepository.findByUserIdAndResource(ownerId, resourceType, resourceId))
                .thenReturn(Optional.of(ownerGrantForOwner));
        
        boolean result = permissionChecker.canRevoke(
                ownerId, resourceType, resourceId, ownerGrantForOwner);
        
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("检查是否可以撤销 - 非Owner不可以撤销")
    void canRevoke_NonOwner_ReturnFalse() {
        UserId editorId = UserId.of(2L);
        ResourceGrant editorGrantForEditor = ResourceGrant.create(
                resourceType, resourceId, editorId, PermissionLevel.EDITOR, UserId.of(1L));
        ResourceGrant grantToRevoke = ResourceGrant.create(
                resourceType, resourceId, UserId.of(3L), PermissionLevel.VIEWER, UserId.of(1L));
        
        when(resourceGrantRepository.findByUserIdAndResource(editorId, resourceType, resourceId))
                .thenReturn(Optional.of(editorGrantForEditor));
        
        boolean result = permissionChecker.canRevoke(
                editorId, resourceType, resourceId, grantToRevoke);
        
        assertThat(result).isFalse();
    }
    
    @Test
    @DisplayName("确保权限 - 有权限通过")
    void ensurePermission_UserHasPermission_NoException() {
        when(resourceGrantRepository.findByUserIdAndResource(userId, resourceType, resourceId))
                .thenReturn(Optional.of(editorGrant));
        
        assertThatCode(() -> permissionChecker.ensurePermission(
                userId, resourceType, resourceId, PermissionLevel.VIEWER))
                .doesNotThrowAnyException();
    }
    
    @Test
    @DisplayName("确保权限 - 无权限抛出异常")
    void ensurePermission_UserHasNoPermission_ThrowException() {
        when(resourceGrantRepository.findByUserIdAndResource(userId, resourceType, resourceId))
                .thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> permissionChecker.ensurePermission(
                userId, resourceType, resourceId, PermissionLevel.VIEWER))
                .isInstanceOf(PermissionChecker.PermissionDeniedException.class)
                .hasMessageContaining("没有资源")
                .hasMessageContaining("权限");
    }
}
