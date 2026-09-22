-- 错题统计查询索引升级脚本
-- 适用数据库：MySQL 8.0+

SET @wrong_question_statistics_index_count = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_wrong_question'
      AND INDEX_NAME = 'idx_wrong_question_statistics_filter'
);
SET @wrong_question_statistics_index_sql = IF(
    @wrong_question_statistics_index_count = 0,
    'ALTER TABLE `sys_wrong_question` ADD KEY `idx_wrong_question_statistics_filter` (`create_id`, `grade`, `subject`, `status`)',
    'SELECT 1'
);
PREPARE wrong_question_statistics_index_stmt FROM @wrong_question_statistics_index_sql;
EXECUTE wrong_question_statistics_index_stmt;
DEALLOCATE PREPARE wrong_question_statistics_index_stmt;
