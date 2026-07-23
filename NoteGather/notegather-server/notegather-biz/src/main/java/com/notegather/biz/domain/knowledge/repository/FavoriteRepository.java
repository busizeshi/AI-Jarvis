package com.notegather.biz.domain.knowledge.repository;

import com.notegather.biz.domain.identity.valueobject.UserId;
import com.notegather.biz.domain.knowledge.entity.Favorite;
import com.notegather.biz.domain.knowledge.valueobject.FavoriteId;
import com.notegather.biz.domain.knowledge.valueobject.ResourceType;

import java.util.List;
import java.util.Optional;

/**
 * 收藏仓储接口
 */
public interface FavoriteRepository {
    
    /**
     * 保存收藏
     */
    Favorite save(Favorite favorite);
    
    /**
     * 根据ID查询收藏
     */
    Optional<Favorite> findById(FavoriteId id);
    
    /**
     * 根据用户ID和资源类型查询收藏列表
     */
    List<Favorite> findByUserIdAndResourceType(UserId userId, ResourceType resourceType);
    
    /**
     * 查询用户的所有收藏
     */
    List<Favorite> findByUserId(UserId userId);
    
    /**
     * 检查是否已收藏
     */
    boolean existsByUserIdAndResource(UserId userId, ResourceType resourceType, Long resourceId);
    
    /**
     * 根据用户ID、资源类型和资源ID查询收藏
     */
    Optional<Favorite> findByUserIdAndResource(UserId userId, ResourceType resourceType, Long resourceId);
    
    /**
     * 删除收藏
     */
    void delete(FavoriteId id);
    
    /**
     * 删除指定资源的所有收藏
     */
    void deleteByResource(ResourceType resourceType, Long resourceId);
}
