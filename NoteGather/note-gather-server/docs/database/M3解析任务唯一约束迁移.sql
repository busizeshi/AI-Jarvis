-- 已执行旧版 phase1_database.sql 的环境需要执行本迁移。
-- 确保同一个文件任一时刻只能存在一条 PENDING 或 PROCESSING 任务。
-- 当前线上 schema 使用 VARCHAR(16) 保存活跃状态值，需与 docs/SQL/notegather_schema.sql 一致。
ALTER TABLE t_parse_task
    DROP INDEX uk_parse_task_active_file,
    DROP COLUMN status_active,
    ADD COLUMN status_active VARCHAR(16) GENERATED ALWAYS AS (
        IF(deleted = 0 AND status IN ('PENDING', 'PROCESSING'), status, NULL)
    ) VIRTUAL COMMENT '活跃任务状态：未删除且进行中时保留状态，否则为NULL',
    ADD UNIQUE KEY uk_parse_task_active_file (file_id, status_active);
