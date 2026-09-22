-- 错题核心接口权限资源补齐脚本
-- 适用数据库：已执行错题菜单初始化脚本的 MySQL 8.0+ 数据库。

SET @wrong_question_resource_id = (SELECT `id` FROM `sys_resource`
                                   WHERE `code` = 'system:wrongQuestion' LIMIT 1);
SET @wrong_question_menu_id = (SELECT `id` FROM `sys_menu`
                               WHERE `code` = 'wrongQuestion' LIMIT 1);

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('错题分页列表', 'system:wrongQuestion:wrongQuestionPageList',
        @wrong_question_resource_id, 69, NOW()),
       ('错题详情', 'system:wrongQuestion:wrongQuestionDetail',
        @wrong_question_resource_id, 70, NOW()),
       ('创建错题', 'system:wrongQuestion:createWrongQuestion',
        @wrong_question_resource_id, 71, NOW()),
       ('图片识别导入错题', 'system:wrongQuestion:importWrongQuestionImage',
        @wrong_question_resource_id, 72, NOW()),
       ('更新错题', 'system:wrongQuestion:updateWrongQuestion',
        @wrong_question_resource_id, 73, NOW()),
       ('更新错题图片', 'system:wrongQuestion:updateWrongQuestionImage',
        @wrong_question_resource_id, 74, NOW()),
       ('删除错题', 'system:wrongQuestion:deleteWrongQuestion',
        @wrong_question_resource_id, 75, NOW()),
       ('批量删除错题', 'system:wrongQuestion:batchDeleteWrongQuestion',
        @wrong_question_resource_id, 76, NOW()),
       ('创建题目采集任务', 'system:wrongQuestion:createQuestionCaptureTask',
        @wrong_question_resource_id, 84, NOW()),
       ('题目采集任务分页列表', 'system:wrongQuestion:questionCaptureTaskPageList',
        @wrong_question_resource_id, 85, NOW()),
       ('题目采集任务详情', 'system:wrongQuestion:questionCaptureTaskDetail',
        @wrong_question_resource_id, 86, NOW()),
       ('重试题目采集任务', 'system:wrongQuestion:retryQuestionCaptureTask',
        @wrong_question_resource_id, 87, NOW()),
       ('修改题目采集题块', 'system:wrongQuestion:updateQuestionCaptureRegion',
        @wrong_question_resource_id, 88, NOW()),
       ('合并题目采集题块', 'system:wrongQuestion:mergeQuestionCaptureRegion',
        @wrong_question_resource_id, 89, NOW()),
       ('拆分题目采集题块', 'system:wrongQuestion:splitQuestionCaptureRegion',
        @wrong_question_resource_id, 90, NOW()),
       ('删除题目采集题块', 'system:wrongQuestion:deleteQuestionCaptureRegion',
        @wrong_question_resource_id, 91, NOW()),
       ('恢复题目采集题块', 'system:wrongQuestion:restoreQuestionCaptureRegion',
        @wrong_question_resource_id, 92, NOW()),
       ('重试题目采集页面', 'system:wrongQuestion:retryQuestionCapturePage',
        @wrong_question_resource_id, 93, NOW()),
       ('确认题目采集', 'system:wrongQuestion:confirmQuestionCapture',
        @wrong_question_resource_id, 94, NOW());

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
SELECT @wrong_question_menu_id, `id`, CONCAT(@wrong_question_resource_id, '-', `id`), NOW()
FROM `sys_resource`
WHERE `code` IN (
    'system:wrongQuestion:wrongQuestionPageList',
    'system:wrongQuestion:wrongQuestionDetail',
    'system:wrongQuestion:createWrongQuestion',
    'system:wrongQuestion:importWrongQuestionImage',
    'system:wrongQuestion:updateWrongQuestion',
    'system:wrongQuestion:updateWrongQuestionImage',
    'system:wrongQuestion:deleteWrongQuestion',
    'system:wrongQuestion:batchDeleteWrongQuestion',
    'system:wrongQuestion:createQuestionCaptureTask',
    'system:wrongQuestion:questionCaptureTaskPageList',
    'system:wrongQuestion:questionCaptureTaskDetail',
    'system:wrongQuestion:retryQuestionCaptureTask',
    'system:wrongQuestion:updateQuestionCaptureRegion',
    'system:wrongQuestion:mergeQuestionCaptureRegion',
    'system:wrongQuestion:splitQuestionCaptureRegion',
    'system:wrongQuestion:deleteQuestionCaptureRegion',
    'system:wrongQuestion:restoreQuestionCaptureRegion',
    'system:wrongQuestion:retryQuestionCapturePage',
    'system:wrongQuestion:confirmQuestionCapture'
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
