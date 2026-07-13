package com.notegather.common.api.user.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息 DTO（Dubbo 跨服务传输对象）
 * <p>
 * ng-admin 通过 Dubbo 调用 ng-biz 的 {@link com.notegather.common.api.user.UserFacade}
 * 获取用户信息时使用。
 */
@Data
public class UserInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID（雪花算法，String 避免 JS 精度丢失） */
    private String userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatar;

    /** 邮箱 */
    private String email;

    /** 状态：0-正常 1-禁用 */
    private Integer status;

    /** 注册时间 */
    private LocalDateTime createTime;
}
