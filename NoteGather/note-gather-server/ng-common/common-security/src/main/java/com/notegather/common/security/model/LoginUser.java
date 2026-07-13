package com.notegather.common.security.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录用户信息（存入 JWT payload 及 UserContext）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    /** 用户ID（雪花算法，转为String避免JS精度丢失） */
    private String userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;
}
