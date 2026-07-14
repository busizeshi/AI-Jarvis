package com.notegather.common.api.user;

import com.notegather.common.api.user.dto.UserInfoDTO;

import java.util.List;

public interface UserFacade {

    UserInfoDTO getUserById(String userId);

    UserInfoDTO getUserByUsername(String username);

    List<UserInfoDTO> getUsersByIds(List<String> userIds);

    /**
     * @param status 1-正常，0-禁用
     */
    boolean updateUserStatus(String userId, Integer status);
}
