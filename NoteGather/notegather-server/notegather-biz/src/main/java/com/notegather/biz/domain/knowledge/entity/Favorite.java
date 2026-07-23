package com.notegather.biz.domain.knowledge.entity;

import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.valueobject.FavoriteId;
import com.notegather.biz.domain.knowledge.valueobject.ResourceType;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 收藏实体
 */
@Getter
public class Favorite {
    
    private FavoriteId id;
    private UserId userId;
    private ResourceType resourceType;
    private Long resourceId;
    private LocalDateTime createdAt;
    
    private Favorite() {
    }
    
    /**
     * 创建收藏（工厂方法）
     */
    public static Favorite create(UserId userId, ResourceType resourceType, Long resourceId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (resourceType == null) {
            throw new IllegalArgumentException("资源类型不能为空");
        }
        if (resourceId == null || resourceId <= 0) {
            throw new IllegalArgumentException("资源ID不能为空或小于等于0");
        }
        
        Favorite favorite = new Favorite();
        favorite.userId = userId;
        favorite.resourceType = resourceType;
        favorite.resourceId = resourceId;
        favorite.createdAt = LocalDateTime.now();
        return favorite;
    }
    
    /**
     * 从数据库重建
     */
    public static Favorite reconstitute(Long id, Long userId, ResourceType resourceType, 
                                       Long resourceId, LocalDateTime createdAt) {
        Favorite favorite = new Favorite();
        favorite.id = FavoriteId.of(id);
        favorite.userId = UserId.of(userId);
        favorite.resourceType = resourceType;
        favorite.resourceId = resourceId;
        favorite.createdAt = createdAt;
        return favorite;
    }
    
    /**
     * 检查是否为指定用户的收藏
     */
    public boolean isFavoritedBy(UserId userId) {
        return this.userId.equals(userId);
    }
    
    /**
     * 设置ID（保存后回填）
     */
    public void setId(Long id) {
        this.id = FavoriteId.of(id);
    }
}
