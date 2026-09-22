-- 第4周：智能复习计划精细化 + 掌握度计算
-- 适用数据库：MySQL 8.0+

SET @schema_name = DATABASE();

SET @sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE `sys_review_item` ADD COLUMN `mastery_score` tinyint unsigned NOT NULL DEFAULT 40 COMMENT ''当前掌握度，0-100'' AFTER `last_feedback`',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'sys_review_item'
      AND COLUMN_NAME = 'mastery_score'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE `sys_review_item` ADD COLUMN `wrong_streak` smallint unsigned NOT NULL DEFAULT 0 COMMENT ''连续错误次数'' AFTER `correct_streak`',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'sys_review_item'
      AND COLUMN_NAME = 'wrong_streak'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE `sys_review_record` ADD COLUMN `mastery_score_before` tinyint unsigned DEFAULT NULL COMMENT ''复习前掌握度，0-100'' AFTER `stage_after`',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'sys_review_record'
      AND COLUMN_NAME = 'mastery_score_before'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE `sys_review_record` ADD COLUMN `mastery_score_after` tinyint unsigned DEFAULT NULL COMMENT ''复习后掌握度，0-100'' AFTER `mastery_score_before`',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'sys_review_record'
      AND COLUMN_NAME = 'mastery_score_after'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE `sys_review_record` ADD COLUMN `mastery_score_delta` smallint DEFAULT NULL COMMENT ''本次掌握度变化'' AFTER `mastery_score_after`',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'sys_review_record'
      AND COLUMN_NAME = 'mastery_score_delta'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE `sys_review_record` ADD COLUMN `correct_streak_after` smallint unsigned DEFAULT NULL COMMENT ''反馈后连续正确次数'' AFTER `mastery_score_delta`',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'sys_review_record'
      AND COLUMN_NAME = 'correct_streak_after'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE `sys_review_record` ADD COLUMN `wrong_streak_after` smallint unsigned DEFAULT NULL COMMENT ''反馈后连续错误次数'' AFTER `correct_streak_after`',
        'SELECT 1')
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'sys_review_record'
      AND COLUMN_NAME = 'wrong_streak_after'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `sys_review_item`
SET `mastery_score` = CASE
        WHEN `mastered_time` IS NOT NULL THEN 90
        WHEN `stage` IS NULL THEN 40
        ELSE LEAST(100, GREATEST(0, 40 + (GREATEST(`stage`, 1) - 1) * 8))
    END,
    `wrong_streak` = IFNULL(`wrong_streak`, 0)
WHERE `mastery_score` IS NULL OR `mastery_score` = 40;

SET @sql = (
    SELECT IF(COUNT(*) = 0,
        'CREATE INDEX `idx_review_item_mastery` ON `sys_review_item` (`user_id`, `item_status`, `mastery_score`, `next_review_time`)',
        'SELECT 1')
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @schema_name
      AND TABLE_NAME = 'sys_review_item'
      AND INDEX_NAME = 'idx_review_item_mastery'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
