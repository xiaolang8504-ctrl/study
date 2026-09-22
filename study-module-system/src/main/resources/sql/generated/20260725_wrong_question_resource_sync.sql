-- 错题模块 API 权限资源全量同步脚本
-- 适用数据库：MySQL 8.0+；可重复执行。

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('初中生错题归档', 'system:wrongQuestion', 0, 60, NOW());

SET @wrong_question_resource_id = (SELECT `id` FROM `sys_resource`
                                   WHERE `code` = 'system:wrongQuestion' LIMIT 1);
SET @wrong_question_menu_id = (SELECT `id` FROM `sys_menu`
                               WHERE `code` = 'wrongQuestion' LIMIT 1);

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('错题分页列表', 'system:wrongQuestion:wrongQuestionPageList', @wrong_question_resource_id, 69, NOW()),
       ('错题详情', 'system:wrongQuestion:wrongQuestionDetail', @wrong_question_resource_id, 70, NOW()),
       ('创建错题', 'system:wrongQuestion:createWrongQuestion', @wrong_question_resource_id, 71, NOW()),
       ('图片识别导入错题', 'system:wrongQuestion:importWrongQuestionImage', @wrong_question_resource_id, 72, NOW()),
       ('更新错题', 'system:wrongQuestion:updateWrongQuestion', @wrong_question_resource_id, 73, NOW()),
       ('更新错题图片', 'system:wrongQuestion:updateWrongQuestionImage', @wrong_question_resource_id, 74, NOW()),
       ('删除错题', 'system:wrongQuestion:deleteWrongQuestion', @wrong_question_resource_id, 75, NOW()),
       ('批量删除错题', 'system:wrongQuestion:batchDeleteWrongQuestion', @wrong_question_resource_id, 76, NOW()),
       ('错题状态流转', 'system:wrongQuestion:updateWrongQuestionStatus', @wrong_question_resource_id, 77, NOW()),
       ('提交错题订正记录', 'system:wrongQuestion:submitCorrectionRecord', @wrong_question_resource_id, 78, NOW()),
       ('错题订正记录列表', 'system:wrongQuestion:correctionRecordList', @wrong_question_resource_id, 79, NOW()),
       ('绑定错题知识点', 'system:wrongQuestion:bindWrongQuestionKnowledgePoint', @wrong_question_resource_id, 80, NOW()),
       ('错题知识点统计', 'system:wrongQuestion:wrongQuestionKnowledgePointStatistics', @wrong_question_resource_id, 81, NOW()),
       ('更新错题错因分析', 'system:wrongQuestion:updateWrongQuestionErrorAnalysis', @wrong_question_resource_id, 82, NOW()),
       ('错因分析统计', 'system:wrongQuestion:wrongQuestionErrorAnalysisStatistics', @wrong_question_resource_id, 83, NOW()),
       ('创建题目采集任务', 'system:wrongQuestion:createQuestionCaptureTask', @wrong_question_resource_id, 84, NOW()),
       ('题目采集任务分页列表', 'system:wrongQuestion:questionCaptureTaskPageList', @wrong_question_resource_id, 85, NOW()),
       ('题目采集任务详情', 'system:wrongQuestion:questionCaptureTaskDetail', @wrong_question_resource_id, 86, NOW()),
       ('重试题目采集任务', 'system:wrongQuestion:retryQuestionCaptureTask', @wrong_question_resource_id, 87, NOW()),
       ('修改题目采集题块', 'system:wrongQuestion:updateQuestionCaptureRegion', @wrong_question_resource_id, 88, NOW()),
       ('新增题目采集题块', 'system:wrongQuestion:createQuestionCaptureRegion', @wrong_question_resource_id, 89, NOW()),
       ('恢复题目采集题块快照', 'system:wrongQuestion:restoreQuestionCaptureRegionSnapshot', @wrong_question_resource_id, 95, NOW()),
       ('合并题目采集题块', 'system:wrongQuestion:mergeQuestionCaptureRegion', @wrong_question_resource_id, 90, NOW()),
       ('拆分题目采集题块', 'system:wrongQuestion:splitQuestionCaptureRegion', @wrong_question_resource_id, 90, NOW()),
       ('删除题目采集题块', 'system:wrongQuestion:deleteQuestionCaptureRegion', @wrong_question_resource_id, 91, NOW()),
       ('恢复题目采集题块', 'system:wrongQuestion:restoreQuestionCaptureRegion', @wrong_question_resource_id, 92, NOW()),
       ('重试题目采集页面', 'system:wrongQuestion:retryQuestionCapturePage', @wrong_question_resource_id, 93, NOW()),
       ('确认题目采集', 'system:wrongQuestion:confirmQuestionCapture', @wrong_question_resource_id, 94, NOW());

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
    'system:wrongQuestion:updateWrongQuestionStatus',
    'system:wrongQuestion:submitCorrectionRecord',
    'system:wrongQuestion:correctionRecordList',
    'system:wrongQuestion:bindWrongQuestionKnowledgePoint',
    'system:wrongQuestion:wrongQuestionKnowledgePointStatistics',
    'system:wrongQuestion:updateWrongQuestionErrorAnalysis',
    'system:wrongQuestion:wrongQuestionErrorAnalysisStatistics',
    'system:wrongQuestion:createQuestionCaptureTask',
    'system:wrongQuestion:questionCaptureTaskPageList',
    'system:wrongQuestion:questionCaptureTaskDetail',
    'system:wrongQuestion:retryQuestionCaptureTask',
    'system:wrongQuestion:updateQuestionCaptureRegion',
    'system:wrongQuestion:createQuestionCaptureRegion',
    'system:wrongQuestion:restoreQuestionCaptureRegionSnapshot',
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
