-- 第5周P1增强：专项练习统计资源
-- 适用数据库：MySQL 8.0+

SET @review_resource_id = (SELECT `id` FROM `sys_resource` WHERE `code` = 'system:review' LIMIT 1);

INSERT IGNORE INTO `sys_resource` (`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES
('专项练习统计', 'system:review:practiceStatistics', @review_resource_id, 115, NOW()),
('批量提交专项练习作答', 'system:review:batchSubmitPracticeAnswer', @review_resource_id, 116, NOW()),
('保存专项练习草稿', 'system:review:savePracticeAnswerDraft', @review_resource_id, 117, NOW());

SET @review_menu_id = (SELECT `id` FROM `sys_menu` WHERE `code` = 'intelligentReview' LIMIT 1);
SET @practice_menu_id = (SELECT `id` FROM `sys_menu` WHERE `code` = 'intelligentReview-practice' LIMIT 1);
SET @practice_statistics_resource_id = (
    SELECT `id` FROM `sys_resource` WHERE `code` = 'system:review:practiceStatistics' LIMIT 1
);
SET @practice_batch_submit_resource_id = (
    SELECT `id` FROM `sys_resource` WHERE `code` = 'system:review:batchSubmitPracticeAnswer' LIMIT 1
);
SET @practice_draft_resource_id = (
    SELECT `id` FROM `sys_resource` WHERE `code` = 'system:review:savePracticeAnswerDraft' LIMIT 1
);

INSERT IGNORE INTO `sys_menu_resource` (`menu_id`, `resource_id`, `resource_level`, `create_time`)
VALUES
(@practice_menu_id, @practice_statistics_resource_id,
 CONCAT(@review_resource_id, '-', @practice_statistics_resource_id), NOW()),
(@practice_menu_id, @practice_batch_submit_resource_id,
 CONCAT(@review_resource_id, '-', @practice_batch_submit_resource_id), NOW()),
(@practice_menu_id, @practice_draft_resource_id,
 CONCAT(@review_resource_id, '-', @practice_draft_resource_id), NOW());

UPDATE `sys_menu`
SET `resource_ids` = CASE
        WHEN `resource_ids` IS NULL OR `resource_ids` = '' THEN @practice_statistics_resource_id
        WHEN FIND_IN_SET(@practice_statistics_resource_id, `resource_ids`) = 0
            THEN CONCAT(`resource_ids`, ',', @practice_statistics_resource_id)
        ELSE `resource_ids`
    END,
    `resource_level` = CASE
        WHEN `resource_level` IS NULL OR `resource_level` = ''
            THEN CONCAT(@review_resource_id, '-', @practice_statistics_resource_id)
        WHEN FIND_IN_SET(CONCAT(@review_resource_id, '-', @practice_statistics_resource_id), `resource_level`) = 0
            THEN CONCAT(`resource_level`, ',', CONCAT(@review_resource_id, '-', @practice_statistics_resource_id))
        ELSE `resource_level`
    END
WHERE `id` = @practice_menu_id;

UPDATE `sys_menu`
SET `resource_ids` = CASE
        WHEN `resource_ids` IS NULL OR `resource_ids` = '' THEN @practice_batch_submit_resource_id
        WHEN FIND_IN_SET(@practice_batch_submit_resource_id, `resource_ids`) = 0
            THEN CONCAT(`resource_ids`, ',', @practice_batch_submit_resource_id)
        ELSE `resource_ids`
    END,
    `resource_level` = CASE
        WHEN `resource_level` IS NULL OR `resource_level` = ''
            THEN CONCAT(@review_resource_id, '-', @practice_batch_submit_resource_id)
        WHEN FIND_IN_SET(CONCAT(@review_resource_id, '-', @practice_batch_submit_resource_id),
                `resource_level`) = 0
            THEN CONCAT(`resource_level`, ',',
                    CONCAT(@review_resource_id, '-', @practice_batch_submit_resource_id))
        ELSE `resource_level`
    END
WHERE `id` = @practice_menu_id;

UPDATE `sys_menu`
SET `resource_ids` = CASE
        WHEN `resource_ids` IS NULL OR `resource_ids` = '' THEN @practice_draft_resource_id
        WHEN FIND_IN_SET(@practice_draft_resource_id, `resource_ids`) = 0
            THEN CONCAT(`resource_ids`, ',', @practice_draft_resource_id)
        ELSE `resource_ids`
    END,
    `resource_level` = CASE
        WHEN `resource_level` IS NULL OR `resource_level` = ''
            THEN CONCAT(@review_resource_id, '-', @practice_draft_resource_id)
        WHEN FIND_IN_SET(CONCAT(@review_resource_id, '-', @practice_draft_resource_id),
                `resource_level`) = 0
            THEN CONCAT(`resource_level`, ',',
                    CONCAT(@review_resource_id, '-', @practice_draft_resource_id))
        ELSE `resource_level`
    END
WHERE `id` = @practice_menu_id;
