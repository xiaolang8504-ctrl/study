-- 智能复习科目维度升级脚本
-- 适用于已经执行过 review.sql 的数据库，仅执行一次。

CREATE TABLE IF NOT EXISTS `sys_review_subject_setting` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '复习科目设置ID',
    `plan_id` bigint NOT NULL COMMENT '复习计划ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `subject` varchar(50) NOT NULL COMMENT '科目字典键值',
    `subject_name` varchar(100) NOT NULL DEFAULT '' COMMENT '科目名称快照',
    `enabled` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否参与智能复习: 0否, 1是',
    `daily_limit` smallint unsigned NOT NULL DEFAULT 5 COMMENT '该科目每日复习题量上限',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_review_subject_plan_subject` (`plan_id`, `subject`),
    KEY `idx_review_subject_user_enabled` (`user_id`, `enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户复习计划科目设置表';

ALTER TABLE `sys_review_record`
    ADD COLUMN `subject` varchar(50) NOT NULL DEFAULT '' COMMENT '复习时科目字典键值快照'
        AFTER `question_title_snapshot`,
    ADD COLUMN `subject_name_snapshot` varchar(100) NOT NULL DEFAULT '' COMMENT '复习时科目名称快照'
        AFTER `subject`,
    ADD KEY `idx_review_record_user_subject_time` (`user_id`, `subject`, `review_time`);

-- 历史记录原先没有科目快照，只能使用当前错题科目进行一次性补齐。
UPDATE `sys_review_record` rr
INNER JOIN `sys_wrong_question` wq ON wq.`id` = rr.`wrong_question_id`
SET rr.`subject` = COALESCE(wq.`subject`, ''),
    rr.`subject_name_snapshot` = COALESCE(wq.`subject_name`, '')
WHERE rr.`subject` = '';

ALTER TABLE `sys_review_reminder`
    ADD COLUMN `subject_summary` varchar(500) NOT NULL DEFAULT '' COMMENT '各科任务数量摘要'
        AFTER `overdue_count`;

ALTER TABLE `sys_review_subject_setting`
    MODIFY COLUMN `daily_limit` smallint unsigned NOT NULL DEFAULT 5
        COMMENT '该科目每日复习题量上限';
