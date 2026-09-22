-- 题目采集 P0 新增接口权限资源，可重复执行。
-- 菜单已有资源较多时，避免 GROUP_CONCAT 默认长度截断 resource_ids。
SET SESSION group_concat_max_len = 8192;

SET @wrong_question_resource_id = (SELECT `id` FROM `sys_resource`
                                   WHERE `code` = 'system:wrongQuestion' LIMIT 1);
SET @wrong_question_menu_id = (SELECT `id` FROM `sys_menu`
                               WHERE `code` = 'wrongQuestion' LIMIT 1);

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('新增题目采集题块', 'system:wrongQuestion:createQuestionCaptureRegion',
        @wrong_question_resource_id, 95, NOW()),
       ('恢复题目采集题块快照', 'system:wrongQuestion:restoreQuestionCaptureRegionSnapshot',
        @wrong_question_resource_id, 96, NOW());

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
SELECT @wrong_question_menu_id, `id`, CONCAT(@wrong_question_resource_id, '-', `id`), NOW()
FROM `sys_resource`
WHERE `code` IN ('system:wrongQuestion:createQuestionCaptureRegion',
                 'system:wrongQuestion:restoreQuestionCaptureRegionSnapshot');

UPDATE `sys_menu` m
SET m.`resource_ids` = (
        SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`)
        FROM `sys_menu_resource` mr WHERE mr.`menu_id` = m.`id`
    ),
    m.`resource_level` = (
        SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`)
        FROM `sys_menu_resource` mr WHERE mr.`menu_id` = m.`id`
    )
WHERE m.`id` = @wrong_question_menu_id;
