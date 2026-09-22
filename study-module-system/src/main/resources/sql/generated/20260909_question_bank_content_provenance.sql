-- P2：题库内容来源与授权追溯。已过期内容不得再用于学生练习或推荐。

SET @column_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_question_bank' AND COLUMN_NAME = 'provider');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE `sys_question_bank` ADD COLUMN `provider` varchar(128) DEFAULT NULL COMMENT ''内容提供方或版权方'' AFTER `source_name`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_question_bank' AND COLUMN_NAME = 'external_id');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE `sys_question_bank` ADD COLUMN `external_id` varchar(128) DEFAULT NULL COMMENT ''外部题目唯一标识'' AFTER `provider`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_question_bank' AND COLUMN_NAME = 'license');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE `sys_question_bank` ADD COLUMN `license` varchar(500) DEFAULT NULL COMMENT ''授权说明、合同编号或适用范围'' AFTER `external_id`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @column_exists = (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_question_bank' AND COLUMN_NAME = 'expire_at');
SET @sql = IF(@column_exists = 0, 'ALTER TABLE `sys_question_bank` ADD COLUMN `expire_at` date DEFAULT NULL COMMENT ''授权到期日'' AFTER `license`', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @index_exists = (SELECT COUNT(1) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sys_question_bank' AND INDEX_NAME = 'idx_question_bank_content_available');
SET @sql = IF(@index_exists = 0, 'ALTER TABLE `sys_question_bank` ADD KEY `idx_question_bank_content_available` (`review_status`, `enable`, `expire_at`)', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
