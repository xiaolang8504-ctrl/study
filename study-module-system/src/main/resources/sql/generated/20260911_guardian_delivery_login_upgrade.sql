-- P2：记录成功登录时间，并将周报的站内与邮件送达状态拆分，支持失败通道补偿重试。
-- 使用 information_schema 判断列是否存在，避免 MySQL DDL 部分成功后重启无法重试。
SET @schema_name = DATABASE();

SELECT COUNT(*) INTO @column_exists FROM information_schema.columns
WHERE table_schema = @schema_name AND table_name = 'sys_user' AND column_name = 'last_login_time';
SET @ddl = IF(@column_exists = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `last_login_time` datetime DEFAULT NULL COMMENT ''最近一次成功登录时间'' AFTER `update_time`',
    'SELECT 1');
PREPARE migration_statement FROM @ddl;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SELECT COUNT(*) INTO @column_exists FROM information_schema.columns
WHERE table_schema = @schema_name AND table_name = 'guardian_weekly_report_subscription' AND column_name = 'site_last_sent_week';
SET @ddl = IF(@column_exists = 0,
    'ALTER TABLE `guardian_weekly_report_subscription` ADD COLUMN `site_last_sent_week` date DEFAULT NULL COMMENT ''站内周报最后成功送达周次'' AFTER `last_sent_week`',
    'SELECT 1');
PREPARE migration_statement FROM @ddl;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SELECT COUNT(*) INTO @column_exists FROM information_schema.columns
WHERE table_schema = @schema_name AND table_name = 'guardian_weekly_report_subscription' AND column_name = 'email_last_sent_week';
SET @ddl = IF(@column_exists = 0,
    'ALTER TABLE `guardian_weekly_report_subscription` ADD COLUMN `email_last_sent_week` date DEFAULT NULL COMMENT ''邮件周报最后成功送达周次'' AFTER `site_last_sent_week`',
    'SELECT 1');
PREPARE migration_statement FROM @ddl;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

-- 旧版本只记录总发送周次；迁移后避免对已完成的周报重复发送。
UPDATE `guardian_weekly_report_subscription`
SET `site_last_sent_week` = `last_sent_week`,
    `email_last_sent_week` = `last_sent_week`
WHERE `last_sent_week` IS NOT NULL;

-- 回滚（会丢弃迁移期间新增的登录与送达记录）：
-- ALTER TABLE `guardian_weekly_report_subscription` DROP COLUMN `email_last_sent_week`, DROP COLUMN `site_last_sent_week`;
-- ALTER TABLE `sys_user` DROP COLUMN `last_login_time`;
