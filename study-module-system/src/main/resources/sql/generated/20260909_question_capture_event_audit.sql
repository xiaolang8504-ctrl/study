-- 题目采集可观测事件。该脚本由 GeneratedSqlMigrationRunner 仅执行一次并记录校验和。
CREATE TABLE IF NOT EXISTS `sys_question_capture_event` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `task_id` bigint NOT NULL COMMENT '采集任务ID',
    `page_id` bigint DEFAULT NULL COMMENT '采集页面ID',
    `region_id` bigint DEFAULT NULL COMMENT '采集题块ID',
    `user_id` bigint DEFAULT NULL COMMENT '任务归属用户ID',
    `file_id` bigint DEFAULT NULL COMMENT '关联文件ID',
    `event_type` varchar(64) NOT NULL COMMENT '事件类型',
    `result` varchar(16) NOT NULL COMMENT '结果：SUCCESS、FAILED、INFO',
    `ocr_provider` varchar(64) DEFAULT NULL COMMENT 'OCR供应商',
    `ocr_model` varchar(128) DEFAULT NULL COMMENT 'OCR模型',
    `region_count` int DEFAULT NULL COMMENT '本次生成或影响题块数',
    `elapsed_millis` bigint DEFAULT NULL COMMENT '耗时毫秒',
    `error_message` varchar(500) DEFAULT NULL COMMENT '失败原因',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_capture_event_task_time` (`task_id`, `create_time`),
    KEY `idx_capture_event_type_time` (`event_type`, `create_time`),
    KEY `idx_capture_event_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目采集关键事件审计';
