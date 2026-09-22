-- 第5周P1增强：专项练习题源扩展
-- 适用数据库：MySQL 8.0+

SET @column_exists = (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_practice_session_question'
      AND COLUMN_NAME = 'question_source'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `sys_practice_session_question` ADD COLUMN `question_source` varchar(30) NOT NULL DEFAULT ''WRONG_QUESTION'' COMMENT ''题目来源：WRONG_QUESTION错题本，QUESTION_BANK题库'' AFTER `wrong_question_id`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_practice_session_question'
      AND COLUMN_NAME = 'bank_question_id'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `sys_practice_session_question` ADD COLUMN `bank_question_id` bigint DEFAULT NULL COMMENT ''题库题目ID'' AFTER `question_source`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_practice_answer_record'
      AND COLUMN_NAME = 'question_source'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `sys_practice_answer_record` ADD COLUMN `question_source` varchar(30) NOT NULL DEFAULT ''WRONG_QUESTION'' COMMENT ''题目来源：WRONG_QUESTION错题本，QUESTION_BANK题库'' AFTER `wrong_question_id`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists = (
    SELECT COUNT(1) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_practice_answer_record'
      AND COLUMN_NAME = 'bank_question_id'
);
SET @sql = IF(@column_exists = 0,
    'ALTER TABLE `sys_practice_answer_record` ADD COLUMN `bank_question_id` bigint DEFAULT NULL COMMENT ''题库题目ID'' AFTER `question_source`',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE `sys_practice_session_question`
    MODIFY COLUMN `wrong_question_id` bigint DEFAULT NULL COMMENT '错题ID';

ALTER TABLE `sys_practice_answer_record`
    MODIFY COLUMN `wrong_question_id` bigint DEFAULT NULL COMMENT '错题ID';

SET @index_exists = (
    SELECT COUNT(1) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_practice_session_question'
      AND INDEX_NAME = 'idx_practice_question_bank'
);
SET @sql = IF(@index_exists = 0,
    'CREATE INDEX `idx_practice_question_bank` ON `sys_practice_session_question` (`bank_question_id`)',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
