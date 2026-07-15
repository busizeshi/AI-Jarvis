package com.notegather.admin.application.user.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserResponse {

    String userId;
    String username;
    String nickname;
    String avatarUrl;
    Integer status;
}
