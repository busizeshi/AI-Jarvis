package com.notegather.common.core.constant;

public final class CommonConstants {

    private CommonConstants() {}

    public static final String APP_NAME = "NoteGather";

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USERNAME = "X-Username";
    public static final String HEADER_TOKEN_JTI = "X-Token-Jti";
    public static final String HEADER_TOKEN_EXPIRES_AT = "X-Token-Expires-At";

    public static final long ACCESS_TOKEN_EXPIRE_MS = 2 * 60 * 60 * 1000L;
    public static final long REFRESH_TOKEN_EXPIRE_MS = 30L * 24 * 60 * 60 * 1000L;
    public static final String REDIS_TOKEN_BLACKLIST_PREFIX = "ng:token:blacklist:";
    public static final String REDIS_ACCESS_TOKEN_PREFIX = "ng:token:access:";
    public static final String REDIS_REFRESH_TOKEN_PREFIX = "ng:token:refresh:";
    public static final String REDIS_REFRESH_LOCK_PREFIX = "token:refresh:";

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public static final int MAX_FOLDER_DEPTH = 5;

    public static final long MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024;
    public static final String MINIO_DEFAULT_BUCKET = "notegather";

    public static final int NOT_DELETED = 0;
    public static final int DELETED = 1;
}
