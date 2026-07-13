package com.notegather.common.core.constant;

/**
 * 系统全局常量
 */
public final class CommonConstants {

    private CommonConstants() {}

    /** 系统名称 */
    public static final String APP_NAME = "NoteGather";

    // ==================== HTTP Header ====================
    /** 鉴权请求头 */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    /** Bearer 前缀 */
    public static final String TOKEN_PREFIX = "Bearer ";
    /** 网关透传用户ID请求头 */
    public static final String HEADER_USER_ID = "X-User-Id";
    /** 网关透传用户名请求头 */
    public static final String HEADER_USERNAME = "X-Username";

    // ==================== Token ====================
    /** Access Token 过期时间（毫秒）：2小时 */
    public static final long ACCESS_TOKEN_EXPIRE_MS = 2 * 60 * 60 * 1000L;
    /** Refresh Token 过期时间（毫秒）：30天 */
    public static final long REFRESH_TOKEN_EXPIRE_MS = 30L * 24 * 60 * 60 * 1000L;
    /** Redis Token 黑名单 key 前缀 */
    public static final String REDIS_TOKEN_BLACKLIST_PREFIX = "ng:token:blacklist:";
    /** Redis Refresh Token key 前缀 */
    public static final String REDIS_REFRESH_TOKEN_PREFIX = "ng:token:refresh:";

    // ==================== 分页默认值 ====================
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    // ==================== 知识库 ====================
    /** 文件夹最大层级深度 */
    public static final int MAX_FOLDER_DEPTH = 5;

    // ==================== 文件 ====================
    /** 单文件上传最大 50MB */
    public static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024;
    /** MinIO 默认 Bucket 名称 */
    public static final String MINIO_DEFAULT_BUCKET = "notegather";

    // ==================== 软删除 ====================
    public static final int NOT_DELETED = 0;
    public static final int DELETED = 1;
}
