-- 第 1 周：专项练习组卷规则与题目入选依据快照
-- 适用数据库：MySQL 8.0+；可重复执行。

SET @column_exists = (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_practice_session'
      AND COLUMN_NAME = 'generation_reason'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `sys_practice_session` ADD COLUMN `generation_reason` varchar(500) DEFAULT NULL COMMENT ''组卷规则快照'' AFTER `title`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_practice_session_question'
      AND COLUMN_NAME = 'source_reason'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `sys_practice_session_question` ADD COLUMN `source_reason` varchar(500) DEFAULT NULL COMMENT ''题目入选依据快照'' AFTER `question_title_snapshot`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_practice_session_question'
      AND COLUMN_NAME = 'error_label_snapshot'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `sys_practice_session_question` ADD COLUMN `error_label_snapshot` varchar(255) DEFAULT NULL COMMENT ''错因标签快照'' AFTER `learning_point`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
