-- =====================================================================
-- 拾笺 NoteGather · 第一阶段数据库初始化脚本（PostgreSQL 16）
-- 说明：
--   1. 主键统一使用雪花算法（Snowflake）由应用层生成，类型 BIGINT。
--   2. 公共字段与 common-mybatis BaseEntity 对齐：
--      create_time / update_time / deleted（0=未删除，1=已删除）。
--   3. 由 docker-compose 挂载至 /docker-entrypoint-initdb.d 首次启动自动执行。
-- =====================================================================

-- 连接到目标库（docker 初始化时已创建 notegather 库，此处显式切换）
\c notegather;

-- ============ 用户表 ============
CREATE TABLE IF NOT EXISTS t_user (
    id           BIGINT       PRIMARY KEY,
    username     VARCHAR(64)  NOT NULL,
    password     VARCHAR(256) NOT NULL,                 -- BCrypt 加密
    nickname     VARCHAR(64),
    avatar_url   VARCHAR(512),
    status       SMALLINT     NOT NULL DEFAULT 1,        -- 1正常 0封禁
    create_time  TIMESTAMP    NOT NULL DEFAULT now(),
    update_time  TIMESTAMP    NOT NULL DEFAULT now(),
    deleted      SMALLINT     NOT NULL DEFAULT 0
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_user_username ON t_user (username) WHERE deleted = 0;
COMMENT ON TABLE t_user IS '用户表';

-- ============ 知识库表 ============
CREATE TABLE IF NOT EXISTS t_library (
    id           BIGINT       PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    name         VARCHAR(256) NOT NULL,
    type         VARCHAR(32)  NOT NULL,                  -- RESOURCE/SCRATCH/NOTE/DIARY
    description  TEXT,
    sort_order   INT          NOT NULL DEFAULT 0,
    create_time  TIMESTAMP    NOT NULL DEFAULT now(),
    update_time  TIMESTAMP    NOT NULL DEFAULT now(),
    deleted      SMALLINT     NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_library_user ON t_library (user_id);
COMMENT ON TABLE t_library IS '知识库表';

-- ============ 笔记/文件夹节点表（树结构）============
CREATE TABLE IF NOT EXISTS t_note (
    id           BIGINT       PRIMARY KEY,
    library_id   BIGINT       NOT NULL,
    parent_id    BIGINT,                                 -- NULL 表示直属知识库
    user_id      BIGINT       NOT NULL,
    node_type    VARCHAR(16)  NOT NULL,                  -- FOLDER/NOTE
    title        VARCHAR(512),
    content      TEXT,                                   -- 块编辑器 JSON
    note_type    VARCHAR(16),                            -- NOTE/SCRATCH/TABLE
    sort_order   INT          NOT NULL DEFAULT 0,
    version      INT          NOT NULL DEFAULT 1,
    parse_status VARCHAR(16)  NOT NULL DEFAULT 'NONE',   -- NONE/PENDING/DONE/FAILED
    create_time  TIMESTAMP    NOT NULL DEFAULT now(),
    update_time  TIMESTAMP    NOT NULL DEFAULT now(),
    deleted      SMALLINT     NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_note_library ON t_note (library_id);
CREATE INDEX IF NOT EXISTS idx_note_parent ON t_note (parent_id);
CREATE INDEX IF NOT EXISTS idx_note_user ON t_note (user_id);
COMMENT ON TABLE t_note IS '笔记/文件夹节点表（树结构）';

-- ============ 文件表 ============
CREATE TABLE IF NOT EXISTS t_file (
    id           BIGINT       PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    library_id   BIGINT,
    note_id      BIGINT,
    file_name    VARCHAR(512),
    file_type    VARCHAR(32),                            -- PDF/IMAGE/AUDIO/TXT/MD...
    object_key   VARCHAR(512),                           -- MinIO 对象键
    bucket       VARCHAR(128),
    size         BIGINT,
    parse_status VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    chunk_count  INT          NOT NULL DEFAULT 0,
    create_time  TIMESTAMP    NOT NULL DEFAULT now(),
    update_time  TIMESTAMP    NOT NULL DEFAULT now(),
    deleted      SMALLINT     NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_file_user ON t_file (user_id);
CREATE INDEX IF NOT EXISTS idx_file_note ON t_file (note_id);
COMMENT ON TABLE t_file IS '文件表';

-- ============ 笔记版本表 ============
CREATE TABLE IF NOT EXISTS t_note_version (
    id           BIGINT       PRIMARY KEY,
    note_id      BIGINT       NOT NULL,
    version      INT          NOT NULL,
    content      TEXT,
    created_by   BIGINT,
    create_time  TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_note_version_note ON t_note_version (note_id);
COMMENT ON TABLE t_note_version IS '笔记版本表';
