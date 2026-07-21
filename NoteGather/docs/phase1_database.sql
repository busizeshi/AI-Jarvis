-- NoteGather 第一阶段数据库脚本
-- 目标数据库：MySQL 8.0（InnoDB 引擎，utf8mb4 编码）
--
-- 执行方式示例：
--   mysql -u root -p < phase1_database.sql
--
-- 约定说明：
--   1. 主键 id 由应用层雪花算法（Snowflake）生成，故不设置数据库自增或默认值。
--   2. deleted 字段遵循 MyBatis-Plus 逻辑删除约定：0=未删除（活跃），1=已删除。
--   3. update_time 使用 ON UPDATE CURRENT_TIMESTAMP(3) 自动维护，兼容直接 SQL 写入与 MyBatis 写入。
--   4. “活跃唯一”约束借助 MySQL 生成列（未删除时取业务值、删除后置 NULL）+ 唯一索引实现。

-- ==================== 数据库 ====================

CREATE DATABASE IF NOT EXISTS notegather
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE notegather;

-- ==================== 身份 / 用户 ====================

CREATE TABLE IF NOT EXISTS t_user (
    id              BIGINT       NOT NULL COMMENT '主键ID，应用层雪花算法生成',
    username        VARCHAR(64)  NOT NULL COMMENT '登录用户名，活跃用户范围内唯一，非空白',
    password        VARCHAR(256) NOT NULL COMMENT 'BCrypt 加密后的密码哈希，非空',
    nickname        VARCHAR(64)  NULL COMMENT '用户昵称，可为空',
    avatar_url      VARCHAR(512) NULL COMMENT '头像URL，可为空',
    status          TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：1=正常，0=封禁',
    create_time     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（毫秒精度）',
    update_time     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（毫秒精度），自动维护',
    deleted         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0=未删除，1=已删除',
    username_active VARCHAR(64)  GENERATED ALWAYS AS (IF(deleted = 0, username, NULL)) VIRTUAL COMMENT '活跃用户名：未删除时=username，已删除时=NULL，仅用于活跃唯一约束',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_username_active (username_active),
    KEY idx_user_status (status, id),
    CONSTRAINT ck_user_username_not_blank CHECK (LENGTH(TRIM(username)) > 0),
    CONSTRAINT ck_user_password_not_blank CHECK (LENGTH(password) > 0),
    CONSTRAINT ck_user_status CHECK (status IN (0, 1)),
    CONSTRAINT ck_user_deleted CHECK (deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='应用用户表：账号与登录凭据，访问令牌/刷新令牌存储于 Redis 不入库';

-- ==================== 知识树：知识库 ====================

CREATE TABLE IF NOT EXISTS t_library (
    id                 BIGINT       NOT NULL COMMENT '主键ID，应用层雪花算法生成',
    user_id            BIGINT       NOT NULL COMMENT '归属用户ID，关联 t_user.id',
    name               VARCHAR(256) NOT NULL COMMENT '知识库名称，同一用户活跃范围内唯一，非空白',
    type               VARCHAR(32)  NOT NULL COMMENT '知识库类型：RESOURCE=资料库，SCRATCH=速记库，NOTE=笔记库，DIARY=日记库',
    description         TEXT         NULL COMMENT '知识库描述，可为空',
    sort_order         INT          NOT NULL DEFAULT 0 COMMENT '排序序号，用于列表拖拽排序，>=0，越小越靠前',
    create_time        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（毫秒精度）',
    update_time        DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（毫秒精度），自动维护',
    deleted            TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0=未删除，1=已删除',
    name_active         VARCHAR(256) GENERATED ALWAYS AS (IF(deleted = 0, name, NULL)) VIRTUAL COMMENT '活跃名称：未删除时=name，已删除时=NULL，仅用于活跃唯一约束',
    PRIMARY KEY (id),
    UNIQUE KEY uk_library_id_user (id, user_id),
    UNIQUE KEY uk_library_user_name_active (user_id, name_active),
    KEY idx_library_user_sort (user_id, sort_order, id),
    CONSTRAINT fk_library_user FOREIGN KEY (user_id)
        REFERENCES t_user (id) ON DELETE RESTRICT,
    CONSTRAINT ck_library_name_not_blank CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT ck_library_type CHECK (type IN ('RESOURCE', 'SCRATCH', 'NOTE', 'DIARY')),
    CONSTRAINT ck_library_sort_order CHECK (sort_order >= 0),
    CONSTRAINT ck_library_deleted CHECK (deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='知识库表：用户拥有的知识库，是笔记/文件夹树的根容器';

-- ==================== 知识树：笔记/文件夹节点 ====================

CREATE TABLE IF NOT EXISTS t_note (
    id           BIGINT       NOT NULL COMMENT '主键ID，应用层雪花算法生成',
    library_id   BIGINT       NOT NULL COMMENT '所属知识库ID，关联 t_library.id',
    parent_id    BIGINT       NULL COMMENT '父节点ID，NULL 表示直属知识库根；关联 t_note.id',
    user_id      BIGINT       NOT NULL COMMENT '归属用户ID，关联 t_user.id（冗余便于按用户查询与外键约束）',
    node_type    VARCHAR(16)  NOT NULL COMMENT '节点类型：FOLDER=文件夹，NOTE=笔记',
    title        VARCHAR(512) NOT NULL COMMENT '节点标题，非空白',
    content      MEDIUMTEXT   NOT NULL COMMENT '笔记正文，块编辑器序列化内容；文件夹为空串',
    note_type    VARCHAR(16)  NULL COMMENT '笔记子类型（仅 NOTE 有值）：NOTE=普通笔记，SCRATCH=速记，TABLE=表格',
    sort_order   INT          NOT NULL DEFAULT 0 COMMENT '同级排序序号，>=0，越小越靠前',
    version      INT          NOT NULL DEFAULT 1 COMMENT '当前版本号，>0，每次保存递增',
    parse_status VARCHAR(16)  NOT NULL DEFAULT 'NONE' COMMENT '笔记内容索引状态：NONE/PENDING/PROCESSING/DONE/FAILED',
    create_time  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（毫秒精度）',
    update_time  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（毫秒精度），自动维护',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0=未删除，1=已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_note_id_library (id, library_id),
    KEY idx_note_library_parent_sort (library_id, parent_id, sort_order, id),
    KEY idx_note_user (user_id, id),
    KEY idx_note_parse_status (parse_status, update_time),
    CONSTRAINT fk_note_library_user FOREIGN KEY (library_id, user_id)
        REFERENCES t_library (id, user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_note_parent FOREIGN KEY (parent_id, library_id)
        REFERENCES t_note (id, library_id) ON DELETE CASCADE,
    CONSTRAINT ck_note_parent_not_self CHECK (parent_id IS NULL OR parent_id <> id),
    CONSTRAINT ck_note_node_type CHECK (node_type IN ('FOLDER', 'NOTE')),
    CONSTRAINT ck_note_type CHECK (
        (node_type = 'FOLDER' AND note_type IS NULL)
        OR (node_type = 'NOTE' AND note_type IN ('NOTE', 'SCRATCH', 'TABLE'))
    ),
    CONSTRAINT ck_note_title_not_blank CHECK (LENGTH(TRIM(title)) > 0),
    CONSTRAINT ck_note_sort_order CHECK (sort_order >= 0),
    CONSTRAINT ck_note_version CHECK (version > 0),
    CONSTRAINT ck_note_parse_status CHECK (
        parse_status IN ('NONE', 'PENDING', 'PROCESSING', 'DONE', 'FAILED')
    ),
    CONSTRAINT ck_note_deleted CHECK (deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='笔记节点表：知识库树中的文件夹与笔记节点，支持多级层级结构';

CREATE TABLE IF NOT EXISTS t_note_version (
    id          BIGINT       NOT NULL COMMENT '主键ID，应用层雪花算法生成',
    note_id     BIGINT       NOT NULL COMMENT '所属笔记ID，关联 t_note.id',
    version     INT          NOT NULL COMMENT '版本号，与 note_id 组合唯一，>0',
    title       VARCHAR(512) NOT NULL COMMENT '该版本的笔记标题快照，非空白',
    content     MEDIUMTEXT   NOT NULL COMMENT '该版本的笔记正文快照',
    created_by  BIGINT       NULL COMMENT '创建该版本的用户ID，关联 t_user.id，用户删除时置 NULL',
    create_time DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '版本创建时间（毫秒精度）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_note_version_number (note_id, version),
    KEY idx_note_version_note_time (note_id, version DESC),
    CONSTRAINT fk_note_version_note FOREIGN KEY (note_id)
        REFERENCES t_note (id) ON DELETE CASCADE,
    CONSTRAINT fk_note_version_creator FOREIGN KEY (created_by)
        REFERENCES t_user (id) ON DELETE SET NULL,
    CONSTRAINT ck_note_version_positive CHECK (version > 0),
    CONSTRAINT ck_note_version_title_not_blank CHECK (LENGTH(TRIM(title)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='笔记版本表：不可变的笔记历史快照，用于编辑历史与回收站恢复';

-- ==================== 对象文件与解析 ====================

CREATE TABLE IF NOT EXISTS t_file (
    id           BIGINT       NOT NULL COMMENT '主键ID，应用层雪花算法生成',
    user_id      BIGINT       NOT NULL COMMENT '上传用户ID，关联 t_user.id',
    library_id   BIGINT       NULL COMMENT '所属知识库ID，关联 t_library.id；可为空表示未关联',
    note_id      BIGINT       NULL COMMENT '所属笔记ID，关联 t_note.id；关联笔记时必须同时关联知识库',
    file_name    VARCHAR(512) NOT NULL COMMENT '原始文件名，非空白',
    file_type    VARCHAR(16)  NOT NULL COMMENT '第一阶段文件类型：PDF / TXT / MD',
    content_type VARCHAR(128) NULL COMMENT 'HTTP Content-Type，可为空',
    object_key   VARCHAR(512) NOT NULL COMMENT 'MinIO 对象键，数据库内永不复用，与 bucket 组合唯一',
    bucket       VARCHAR(128) NOT NULL DEFAULT 'notegather' COMMENT 'MinIO 存储桶名称',
    size         BIGINT       NOT NULL COMMENT '文件大小（字节），>=0',
    parse_status VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '解析状态：PENDING/PROCESSING/DONE/FAILED',
    chunk_count  INT          NOT NULL DEFAULT 0 COMMENT '已切分块数，>=0',
    parse_error  TEXT         NULL COMMENT '最近一次解析失败原因，成功时为空',
    create_time  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（毫秒精度）',
    update_time  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（毫秒精度），自动维护',
    deleted      TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0=未删除，1=已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_object (bucket, object_key),
    KEY idx_file_user_library (user_id, library_id, update_time DESC),
    KEY idx_file_note (note_id, id),
    KEY idx_file_parse_status (parse_status, update_time),
    CONSTRAINT fk_file_user FOREIGN KEY (user_id)
        REFERENCES t_user (id) ON DELETE RESTRICT,
    CONSTRAINT fk_file_library_user FOREIGN KEY (library_id, user_id)
        REFERENCES t_library (id, user_id) ON DELETE RESTRICT,
    CONSTRAINT fk_file_note_library FOREIGN KEY (note_id, library_id)
        REFERENCES t_note (id, library_id) ON DELETE RESTRICT,
    CONSTRAINT ck_file_name_not_blank CHECK (LENGTH(TRIM(file_name)) > 0),
    CONSTRAINT ck_file_type CHECK (file_type IN ('PDF', 'TXT', 'MD')),
    CONSTRAINT ck_file_size CHECK (size >= 0),
    CONSTRAINT ck_file_parse_status CHECK (
        parse_status IN ('PENDING', 'PROCESSING', 'DONE', 'FAILED')
    ),
    CONSTRAINT ck_file_chunk_count CHECK (chunk_count >= 0),
    CONSTRAINT ck_file_deleted CHECK (deleted IN (0, 1)),
    CONSTRAINT ck_file_library_required_for_note CHECK (
        note_id IS NULL OR library_id IS NOT NULL
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='文件表：上传到 MinIO 的对象文件，关联到知识库或笔记';

CREATE TABLE IF NOT EXISTS t_parse_task (
    id            BIGINT       NOT NULL COMMENT '主键ID，应用层雪花算法生成',
    file_id       BIGINT       NOT NULL COMMENT '关联文件ID，t_file.id',
    requested_by  BIGINT       NULL COMMENT '发起解析的用户ID，关联 t_user.id，用户删除时置 NULL',
    status        VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT '解析任务状态：PENDING/PROCESSING/DONE/FAILED',
    attempt_count INT          NOT NULL DEFAULT 0 COMMENT '尝试次数，>=0',
    chunk_count   INT          NOT NULL DEFAULT 0 COMMENT '本次解析产出的块数，>=0',
    error_msg     TEXT         NULL COMMENT '最近一次失败错误信息，成功时为空',
    started_at    DATETIME(3)  NULL COMMENT '任务开始时间',
    finished_at   DATETIME(3)  NULL COMMENT '任务结束时间，必须晚于或等于 started_at',
    create_time   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（毫秒精度）',
    update_time   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（毫秒精度），自动维护',
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0=未删除，1=已删除',
    status_active VARCHAR(16)  GENERATED ALWAYS AS (
        IF(deleted = 0 AND status IN ('PENDING', 'PROCESSING'), status, NULL)
    ) VIRTUAL COMMENT '活跃任务状态：未删除且进行中时保留状态，否则为NULL，用于防并发唯一约束',
    PRIMARY KEY (id),
    UNIQUE KEY uk_parse_task_active_file (file_id, status_active),
    KEY idx_parse_task_file_time (file_id, create_time DESC),
    CONSTRAINT fk_parse_task_file FOREIGN KEY (file_id)
        REFERENCES t_file (id) ON DELETE CASCADE,
    CONSTRAINT fk_parse_task_requester FOREIGN KEY (requested_by)
        REFERENCES t_user (id) ON DELETE SET NULL,
    CONSTRAINT ck_parse_task_status CHECK (
        status IN ('PENDING', 'PROCESSING', 'DONE', 'FAILED')
    ),
    CONSTRAINT ck_parse_task_attempts CHECK (attempt_count >= 0),
    CONSTRAINT ck_parse_task_chunks CHECK (chunk_count >= 0),
    CONSTRAINT ck_parse_task_times CHECK (
        finished_at IS NULL OR started_at IS NULL OR finished_at >= started_at
    ),
    CONSTRAINT ck_parse_task_deleted CHECK (deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='解析任务表：文件解析的执行历史；t_file 仅保存当前聚合状态';

-- ==================== RAG 问答 ====================

CREATE TABLE IF NOT EXISTS t_chat_session (
    id          BIGINT       NOT NULL COMMENT '主键ID，应用层雪花算法生成',
    user_id     BIGINT       NOT NULL COMMENT '归属用户ID，关联 t_user.id',
    title       VARCHAR(256) NULL COMMENT '会话标题，由首条问答自动生成或手动修改；为空时前端展示默认标题',
    create_time DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（毫秒精度）',
    update_time DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（毫秒精度），自动维护',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0=未删除，1=已删除',
    PRIMARY KEY (id),
    KEY idx_chat_session_user_time (user_id, update_time DESC, id),
    CONSTRAINT fk_chat_session_user FOREIGN KEY (user_id)
        REFERENCES t_user (id) ON DELETE RESTRICT,
    CONSTRAINT ck_chat_session_title CHECK (
        title IS NULL OR LENGTH(TRIM(title)) > 0
    ),
    CONSTRAINT ck_chat_session_deleted CHECK (deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='RAG 问答会话表：用户拥有的对话会话';

CREATE TABLE IF NOT EXISTS t_chat_message (
    id          BIGINT       NOT NULL COMMENT '主键ID，应用层雪花算法生成',
    session_id  BIGINT       NOT NULL COMMENT '所属会话ID，关联 t_chat_session.id',
    message_no  INT          NOT NULL COMMENT '消息序号（会话内），>0，同一会话内唯一',
    role        VARCHAR(16)  NOT NULL COMMENT '消息角色：USER=用户提问，ASSISTANT=助手回答，SYSTEM=系统提示',
    content     MEDIUMTEXT   NOT NULL COMMENT '消息正文，流式回答中逐步填充；默认空串',
    status      VARCHAR(16)  NOT NULL DEFAULT 'COMPLETED' COMMENT '消息状态：STREAMING=流式中，COMPLETED=已完成，FAILED=失败',
    error_msg   TEXT         NULL COMMENT '失败时的错误信息，成功/流式中为空',
    create_time DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（毫秒精度）',
    update_time DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间（毫秒精度），自动维护',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0=未删除，1=已删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_chat_message_number (session_id, message_no),
    KEY idx_chat_message_session_number (session_id, message_no),
    CONSTRAINT fk_chat_message_session FOREIGN KEY (session_id)
        REFERENCES t_chat_session (id) ON DELETE CASCADE,
    CONSTRAINT ck_chat_message_number CHECK (message_no > 0),
    CONSTRAINT ck_chat_message_role CHECK (role IN ('USER', 'ASSISTANT', 'SYSTEM')),
    CONSTRAINT ck_chat_message_status CHECK (
        status IN ('STREAMING', 'COMPLETED', 'FAILED')
    ),
    CONSTRAINT ck_chat_message_deleted CHECK (deleted IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='RAG 问答消息表：持久化的用户提问与流式助手回答';

CREATE TABLE IF NOT EXISTS t_chat_citation (
    id             BIGINT       NOT NULL COMMENT '主键ID，应用层雪花算法生成',
    message_id     BIGINT       NOT NULL COMMENT '所属消息ID，关联 t_chat_message.id（仅 ASSISTANT 消息）',
    note_id        BIGINT       NULL COMMENT '引用笔记ID，关联 t_note.id；笔记删除时置 NULL',
    citation_order INT          NOT NULL COMMENT '引用排序序号，>=0，同一消息内唯一',
    note_title     VARCHAR(512) NOT NULL COMMENT '笔记标题快照：回答时记录，后续重命名不影响历史展示；非空白',
    chunk_text     TEXT         NOT NULL COMMENT '引用文本块：检索命中的笔记片段，非空白',
    score          REAL         NULL COMMENT '检索相关度得分，越高越相关，可为空',
    create_time    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间（毫秒精度）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_chat_citation_order (message_id, citation_order),
    KEY idx_chat_citation_message_order (message_id, citation_order),
    KEY idx_chat_citation_note (note_id),
    CONSTRAINT fk_chat_citation_message FOREIGN KEY (message_id)
        REFERENCES t_chat_message (id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_citation_note FOREIGN KEY (note_id)
        REFERENCES t_note (id) ON DELETE SET NULL,
    CONSTRAINT ck_chat_citation_order CHECK (citation_order >= 0),
    CONSTRAINT ck_chat_citation_title CHECK (LENGTH(TRIM(note_title)) > 0),
    CONSTRAINT ck_chat_citation_text CHECK (LENGTH(chunk_text) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='RAG 问答引用表：与回答一起返回的来源引用快照，保障历史回答可追溯';
