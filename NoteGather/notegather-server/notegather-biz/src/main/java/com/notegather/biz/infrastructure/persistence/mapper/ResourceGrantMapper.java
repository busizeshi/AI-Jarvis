package com.notegather.biz.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.notegather.biz.infrastructure.persistence.entity.ResourceGrantEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 资源授权Mapper
 */
@Mapper
public interface ResourceGrantMapper extends BaseMapper<ResourceGrantEntity> {
    
    /**
     * 根据用户ID和资源查询授权
     */
    ResourceGrantEntity selectByUserIdAndResource(
            @Param("userId") Long userId,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId);
    
    /**
     * 查询资源的所有授权
     */
    List<ResourceGrantEntity> selectByResource(
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId);
    
    /**
     * 查询资源的指定权限级别的授权列表
     */
    List<ResourceGrantEntity> selectByResourceAndPermissionLevel(
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId,
            @Param("permissionLevel") String permissionLevel);
    
    /**
     * 查询用户的所有授权
     */
    List<ResourceGrantEntity> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户对指定资源类型的授权
     */
    List<ResourceGrantEntity> selectByUserIdAndResourceType(
            @Param("userId") Long userId,
            @Param("resourceType") String resourceType);
    
    /**
     * 检查授权是否存在
     */
    int countByUserIdAndResource(
            @Param("userId") Long userId,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId);
    
    /**
     * 删除资源的所有授权
     */
    int deleteByResource(
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId);
    
    /**
     * 删除用户对资源的授权
     */
    int deleteByUserIdAndResource(
            @Param("userId") Long userId,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId);
    
    /**
     * 查询资源的 Owner
     */
    ResourceGrantEntity selectOwnerByResource(
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId);
    
    /**
     * 统计资源的成员数量
     */
    long countByResource(
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId);
}
