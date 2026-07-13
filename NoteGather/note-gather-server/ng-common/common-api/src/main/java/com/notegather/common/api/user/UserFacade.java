package com.notegather.common.api.user;

import com.notegather.common.api.user.dto.UserInfoDTO;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 用户服务 Dubbo Facade 接口
 * <p>
 * 服务提供方（ng-biz）实现此接口并使用 {@link DubboService} 暴露；
 * 服务消费方（ng-admin 等）使用 {@code @DubboReference} 注入后调用。
 */
public interface UserFacade {

    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息，不存在时返回 null
     */
    UserInfoDTO getUserById(String userId);

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户信息，不存在时返回 null
     */
    UserInfoDTO getUserByUsername(String username);

    /**
     * 批量查询用户信息（管理端用户列表使用）
     *
     * @param userIds 用户ID列表
     * @return 用户信息列表，不存在的用户不包含在结果中
     */
    java.util.List<UserInfoDTO> getUsersByIds(java.util.List<String> userIds);

    /**
     * 修改用户状态（禁用/启用）
     *
     * @param userId 用户ID
     * @param status 0-正常 1-禁用
     * @return 是否操作成功
     */
    boolean updateUserStatus(String userId, Integer status);
}
