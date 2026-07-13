package com.notegather.common.core.result;

/**
 * 统一响应码枚举
 */
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    CREATED(201, "创建成功"),

    // 客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无访问权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    // 服务端错误
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 业务错误 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户名已被注册"),
    PASSWORD_ERROR(1003, "密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被禁用"),
    TOKEN_INVALID(1005, "Token 无效"),
    TOKEN_EXPIRED(1006, "Token 已过期"),
    REFRESH_TOKEN_INVALID(1007, "刷新 Token 无效或已过期"),

    // 知识库 2xxx
    LIBRARY_NOT_FOUND(2001, "知识库不存在"),
    NOTE_NOT_FOUND(2002, "笔记不存在"),
    FOLDER_MAX_DEPTH(2003, "文件夹层级已达上限（最多5层）"),
    NOTE_DELETED(2004, "笔记已被删除"),

    // 文件 3xxx
    FILE_UPLOAD_FAIL(3001, "文件上传失败"),
    FILE_NOT_FOUND(3002, "文件不存在"),
    FILE_TYPE_NOT_SUPPORT(3003, "不支持的文件类型"),
    FILE_SIZE_EXCEED(3004, "文件大小超出限制");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
