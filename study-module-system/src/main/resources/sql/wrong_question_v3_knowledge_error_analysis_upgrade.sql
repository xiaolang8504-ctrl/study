-- 错题知识点绑定与错因分析升级脚本
-- 适用数据库：MySQL 8.0+

SET @wrong_question_resource_id = (SELECT `id` FROM `sys_resource`
                                   WHERE `code` = 'system:wrongQuestion' LIMIT 1);
SET @wrong_question_menu_id = (SELECT `id` FROM `sys_menu`
                               WHERE `code` = 'wrongQuestion' LIMIT 1);

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('错题状态流转', 'system:wrongQuestion:updateWrongQuestionStatus',
        @wrong_question_resource_id, 77, NOW()),
       ('提交错题订正记录', 'system:wrongQuestion:submitCorrectionRecord',
        @wrong_question_resource_id, 78, NOW()),
       ('错题订正记录列表', 'system:wrongQuestion:correctionRecordList',
        @wrong_question_resource_id, 79, NOW()),
       ('绑定错题知识点', 'system:wrongQuestion:bindWrongQuestionKnowledgePoint',
        @wrong_question_resource_id, 80, NOW()),
       ('错题知识点统计', 'system:wrongQuestion:wrongQuestionKnowledgePointStatistics',
        @wrong_question_resource_id, 81, NOW()),
       ('更新错题错因分析', 'system:wrongQuestion:updateWrongQuestionErrorAnalysis',
        @wrong_question_resource_id, 82, NOW()),
       ('错因分析统计', 'system:wrongQuestion:wrongQuestionErrorAnalysisStatistics',
        @wrong_question_resource_id, 83, NOW());

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
SELECT @wrong_question_menu_id, `id`, CONCAT(@wrong_question_resource_id, '-', `id`), NOW()
FROM `sys_resource`
WHERE `code` IN (
    'system:wrongQuestion:updateWrongQuestionStatus',
    'system:wrongQuestion:submitCorrectionRecord',
    'system:wrongQuestion:correctionRecordList',
    'system:wrongQuestion:bindWrongQuestionKnowledgePoint',
    'system:wrongQuestion:wrongQuestionKnowledgePointStatistics',
    'system:wrongQuestion:updateWrongQuestionErrorAnalysis',
    'system:wrongQuestion:wrongQuestionErrorAnalysisStatistics'
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

INSERT IGNORE INTO `sys_dict`
(`dict_type`, `dict_name`, `remark`, `create_time`, `update_time`)
VALUES ('wrong_question_error_label', '错因分类', '错题错因分析标签', NOW(), NOW());

INSERT IGNORE INTO `sys_dict_data`
(`dict_label`, `dict_type`, `dict_value`, `dict_data_sort`, `is_enable`, `remark`, `create_time`, `update_time`)
VALUES ('概念不清', 'wrong_question_error_label', 'CONCEPT_UNCLEAR', 1, 1, '', NOW(), NOW()),
       ('审题错误', 'wrong_question_error_label', 'MISREAD_QUESTION', 2, 1, '', NOW(), NOW()),
       ('计算错误', 'wrong_question_error_label', 'CALCULATION_ERROR', 3, 1, '', NOW(), NOW()),
       ('公式不会', 'wrong_question_error_label', 'FORMULA_UNKNOWN', 4, 1, '', NOW(), NOW()),
       ('步骤不完整', 'wrong_question_error_label', 'INCOMPLETE_STEPS', 5, 1, '', NOW(), NOW()),
       ('粗心', 'wrong_question_error_label', 'CARELESS', 6, 1, '', NOW(), NOW()),
       ('不会做', 'wrong_question_error_label', 'UNKNOWN_METHOD', 7, 1, '', NOW(), NOW());

SET @wrong_question_error_label_index_count = (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'sys_wrong_question'
      AND INDEX_NAME = 'idx_wrong_question_error_labels'
);
SET @wrong_question_error_label_index_sql = IF(
    @wrong_question_error_label_index_count = 0,
    'ALTER TABLE `sys_wrong_question` ADD KEY `idx_wrong_question_error_labels` (`error_labels`)',
    'SELECT 1'
);
PREPARE wrong_question_error_label_index_stmt FROM @wrong_question_error_label_index_sql;
EXECUTE wrong_question_error_label_index_stmt;
DEALLOCATE PREPARE wrong_question_error_label_index_stmt;
