package com.notegather.common.security.context;

import com.notegather.common.security.model.LoginUser;

public final class UserContext {

    private static final ThreadLocal<LoginUser> USER_HOLDER = new ThreadLocal<>();

    private UserContext() {}

    public static void set(LoginUser loginUser) {
        USER_HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return USER_HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = requireLoginUser();
        return Long.parseLong(user.getUserId());
    }

    public static String getUsername() {
        LoginUser user = get();
        return user != null ? user.getUsername() : null;
    }

    public static LoginUser requireLoginUser() {
        LoginUser user = get();
        if (user == null) {
            throw new IllegalStateException("当前请求上下文无登录用户信息");
        }
        return user;
    }

    public static void clear() {
        USER_HOLDER.remove();
    }
}
