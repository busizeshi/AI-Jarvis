package com.notegather.admin.adapter.rpc.user;

import com.notegather.admin.application.user.assembler.UserAssembler;
import com.notegather.admin.application.user.dto.UserResponse;
import com.notegather.admin.application.user.service.UserService;
import com.notegather.common.api.user.UserFacade;
import com.notegather.common.api.user.dto.UserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.List;

@DubboService
@RequiredArgsConstructor
public class UserFacadeImpl implements UserFacade {

    private final UserService userService;

    @Override
    public UserInfoDTO getUserById(String userId) {
        Long id = parseId(userId);
        if (id == null) {
            return null;
        }
        return UserAssembler.toInfoDTO(userService.getById(id));
    }

    @Override
    public UserInfoDTO getUserByUsername(String username) {
        UserResponse response = userService.getByUsername(username);
        if (response == null) {
            return null;
        }
        return UserAssembler.toInfoDTO(response);
    }

    @Override
    public List<UserInfoDTO> getUsersByIds(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        List<Long> ids = userIds.stream()
                .map(this::parseId)
                .filter(java.util.Objects::nonNull)
                .toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        return userService.listByIds(ids).stream()
                .map(UserAssembler::toInfoDTO)
                .toList();
    }

    @Override
    public boolean updateUserStatus(String userId, Integer status) {
        Long id = parseId(userId);
        return id != null && userService.updateStatus(id, status);
    }

    private Long parseId(String userId) {
        try {
            return userId == null ? null : Long.valueOf(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
