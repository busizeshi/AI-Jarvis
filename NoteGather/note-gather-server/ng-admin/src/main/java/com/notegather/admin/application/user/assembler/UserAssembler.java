package com.notegather.admin.application.user.assembler;

import com.notegather.admin.application.user.dto.UserResponse;
import com.notegather.admin.domain.user.model.User;
import com.notegather.common.api.user.dto.UserInfoDTO;

public final class UserAssembler {

    private UserAssembler() {}

    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        String createTimeStr = user.getCreateTime() != null
                ? user.getCreateTime().toString() + "Z"
                : null;
        return UserResponse.builder()
                .userId(String.valueOf(user.getId()))
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .status(user.getStatus())
                .createTime(createTimeStr)
                .build();
    }

    public static UserInfoDTO toInfoDTO(User user) {
        if (user == null) {
            return null;
        }
        UserInfoDTO dto = new UserInfoDTO();
        dto.setUserId(String.valueOf(user.getId()));
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatarUrl());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }

    public static UserInfoDTO toInfoDTO(UserResponse user) {
        if (user == null) {
            return null;
        }
        UserInfoDTO dto = new UserInfoDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setAvatar(user.getAvatarUrl());
        dto.setStatus(user.getStatus());
        return dto;
    }
}
