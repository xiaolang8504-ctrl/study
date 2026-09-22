-- 采集中心 P0 新接口权限资源。
-- 执行后，已登录用户需要重新登录（或刷新 Redis 权限缓存）才能获得新增接口权限。

SET @wrong_question_resource_id = (SELECT `id` FROM `sys_resource`
                                   WHERE `code` = 'system:wrongQuestion' LIMIT 1);
SET @wrong_question_menu_id = (SELECT `id` FROM `sys_menu`
                               WHERE `code` = 'wrongQuestion' LIMIT 1);

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('仅保存采集页面图片', 'system:wrongQuestion:saveQuestionCapturePageAsImage',
        @wrong_question_resource_id, 96, NOW()),
       ('采集题块重复错题检查', 'system:wrongQuestion:questionCaptureDuplicateList',
        @wrong_question_resource_id, 97, NOW());

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
SELECT @wrong_question_menu_id, `id`, CONCAT(@wrong_question_resource_id, '-', `id`), NOW()
FROM `sys_resource`
WHERE `code` IN (
    'system:wrongQuestion:saveQuestionCapturePageAsImage',
    'system:wrongQuestion:questionCaptureDuplicateList'
);

UPDATE `sys_menu` m
SET m.`resource_ids` = (
        SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`)
        FROM `sys_menu_resource` mr
        WHERE mr.`menu_id` = m.`id`
    ),
    m.`resource_level` = (
        SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`)
        FROM `sys_menu_resource` mr
        WHERE mr.`menu_id` = m.`id`
    )
WHERE m.`id` = @wrong_question_menu_id;
