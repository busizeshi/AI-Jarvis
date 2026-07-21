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
    String bio;
    Integer status;
    /** 注册时间，ISO-8601 格式，前端用于展示"加入时间" */
    String createTime;
}
