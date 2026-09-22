-- 错题状态流转与订正记录升级脚本
-- 适用数据库：MySQL 8.0+

SET @wrong_question_status_column_count = (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_wrong_question'
      AND COLUMN_NAME = 'status'
);
SET @wrong_question_status_column_sql = IF(
    @wrong_question_status_column_count = 0,
    'ALTER TABLE `sys_wrong_question` ADD COLUMN `status` tinyint NOT NULL DEFAULT 0 COMMENT ''状态: 0待订正, 1已订正, 2已掌握, 3已归档''',
    'SELECT 1'
);
PREPARE wrong_question_status_column_stmt FROM @wrong_question_status_column_sql;
EXECUTE wrong_question_status_column_stmt;
DEALLOCATE PREPARE wrong_question_status_column_stmt;

SET @wrong_question_status_index_count = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_wrong_question'
      AND INDEX_NAME = 'idx_wrong_question_status'
);
SET @wrong_question_status_index_sql = IF(
    @wrong_question_status_index_count = 0,
    'ALTER TABLE `sys_wrong_question` ADD KEY `idx_wrong_question_status` (`status`)',
    'SELECT 1'
);
PREPARE wrong_question_status_index_stmt FROM @wrong_question_status_index_sql;
EXECUTE wrong_question_status_index_stmt;
DEALLOCATE PREPARE wrong_question_status_index_stmt;

CREATE TABLE IF NOT EXISTS `sys_wrong_question_correction_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订正记录ID',
    `wrong_question_id` bigint NOT NULL COMMENT '错题ID',
    `correction_answer` text DEFAULT NULL COMMENT '订正答案',
    `correction_analysis` text DEFAULT NULL COMMENT '订正解析',
    `correction_image_url` varchar(255) DEFAULT NULL COMMENT '订正图片地址',
    `correction_remark` varchar(500) DEFAULT NULL COMMENT '订正备注',
    `before_status` tinyint DEFAULT NULL COMMENT '订正前状态',
    `after_status` tinyint NOT NULL DEFAULT 1 COMMENT '订正后状态',
    `create_id` bigint NOT NULL COMMENT '创建人ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_wrong_question_correction_wrong_question_id` (`wrong_question_id`),
    KEY `idx_wrong_question_correction_create_id` (`create_id`),
    KEY `idx_wrong_question_correction_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题订正记录表';
