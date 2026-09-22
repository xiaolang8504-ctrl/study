-- 典型错题练习 API 资源升级
-- 执行后请重新登录，使 Redis 权限缓存包含新资源。

SET @review_resource_id = (SELECT `id` FROM `sys_resource`
                           WHERE `code` = 'system:review' LIMIT 1);
SET @review_overview_menu_id = (SELECT `id` FROM `sys_menu`
                                WHERE `code` = 'intelligentReview-home' LIMIT 1);

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('生成典型错题练习', 'system:review:generateTypicalPractice',
        @review_resource_id, 94, NOW());

SET @typical_practice_resource_id = (SELECT `id` FROM `sys_resource`
                                     WHERE `code` = 'system:review:generateTypicalPractice' LIMIT 1);
SET @typical_practice_resource_level = CONCAT(@review_resource_id, '-',
                                              @typical_practice_resource_id);

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
VALUES (@review_overview_menu_id, @typical_practice_resource_id,
        @typical_practice_resource_level, NOW());

UPDATE `sys_menu`
SET `resource_ids` = CASE
        WHEN FIND_IN_SET(
                CONVERT(@typical_practice_resource_id USING utf8mb4) COLLATE utf8mb4_general_ci,
                CONVERT(`resource_ids` USING utf8mb4) COLLATE utf8mb4_general_ci
             ) > 0 THEN `resource_ids`
        ELSE CONCAT_WS(',', NULLIF(`resource_ids`, ''), @typical_practice_resource_id)
    END,
    `resource_level` = CASE
        WHEN FIND_IN_SET(
                CONVERT(@typical_practice_resource_level USING utf8mb4) COLLATE utf8mb4_general_ci,
                CONVERT(`resource_level` USING utf8mb4) COLLATE utf8mb4_general_ci
             ) > 0 THEN `resource_level`
        ELSE CONCAT_WS(',', NULLIF(`resource_level`, ''), @typical_practice_resource_level)
    END
WHERE `id` = @review_overview_menu_id;
