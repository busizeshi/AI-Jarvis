-- 已执行旧版 phase1_database.sql 的环境需要执行本迁移。
-- 确保同一个文件任一时刻只能存在一条 PENDING 或 PROCESSING 任务。
ALTER TABLE t_parse_task
    DROP INDEX uk_parse_task_active_file,
    DROP COLUMN status_active,
    ADD COLUMN status_active TINYINT GENERATED ALWAYS AS (
        IF(deleted = 0 AND status IN ('PENDING', 'PROCESSING'), 1, NULL)
    ) VIRTUAL COMMENT '活跃任务标记：未删除且进行中时固定为1，否则为NULL',
    ADD UNIQUE KEY uk_parse_task_active_file (file_id, status_active);
