package com.notegather.common.security.context;

import com.notegather.common.security.model.LoginUser;

/**
 * 用户上下文（ThreadLocal 持有当前请求的登录用户信息）
 * 由下游服务（biz/admin）在 Filter/Interceptor 中从请求头取出并设置
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> USER_HOLDER = new InheritableThreadLocal<>();

    private UserContext() {}

    public static void set(LoginUser loginUser) {
        USER_HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return USER_HOLDER.get();
    }

    /** 获取当前用户ID，未登录时抛出 IllegalStateException */
    public static Long getUserId() {
        LoginUser user = get();
        if (user == null) {
            throw new IllegalStateException("当前请求上下文无登录用户信息");
        }
        return Long.parseLong(user.getUserId());
    }

    /** 获取当前用户名 */
    public static String getUsername() {
        LoginUser user = get();
        return user != null ? user.getUsername() : null;
    }

    /** 请求处理完毕后清除，防止内存泄漏（在 Filter 的 finally 块调用） */
    public static void clear() {
        USER_HOLDER.remove();
    }
}
