package com.notegather.common.api.user.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class UserInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
    /**
     * 1-正常，0-禁用
     */
    private Integer status;
    private LocalDateTime createTime;
}
