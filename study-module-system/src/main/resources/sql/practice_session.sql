-- 第5周：练习闭环增强 + 专项练习记录
-- 适用数据库：MySQL 8.0+

CREATE TABLE IF NOT EXISTS `sys_practice_session` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '练习会话ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `practice_type` varchar(30) NOT NULL COMMENT '练习类型：KNOWLEDGE知识点，ERROR_LABEL错因，TYPICAL典型错题',
    `title` varchar(100) NOT NULL DEFAULT '' COMMENT '练习标题',
    `subject` varchar(50) DEFAULT NULL COMMENT '科目字典键值',
    `subject_name` varchar(100) NOT NULL DEFAULT '' COMMENT '科目名称快照',
    `learning_point` varchar(255) DEFAULT NULL COMMENT '知识点',
    `error_label` varchar(100) DEFAULT NULL COMMENT '错因标签',
    `difficulty` tinyint DEFAULT NULL COMMENT '难度',
    `question_count` int unsigned NOT NULL DEFAULT 0 COMMENT '题目数量',
    `answered_count` int unsigned NOT NULL DEFAULT 0 COMMENT '已答数量',
    `correct_count` int unsigned NOT NULL DEFAULT 0 COMMENT '正确数量',
    `wrong_count` int unsigned NOT NULL DEFAULT 0 COMMENT '错误数量',
    `total_duration_seconds` int unsigned NOT NULL DEFAULT 0 COMMENT '总用时，秒',
    `accuracy_rate` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '正确率，0-100',
    `status` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '状态：0进行中，1已完成',
    `start_time` datetime NOT NULL COMMENT '开始时间',
    `finish_time` datetime DEFAULT NULL COMMENT '完成时间',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_practice_session_user_time` (`user_id`, `create_time`),
    KEY `idx_practice_session_user_type` (`user_id`, `practice_type`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='专项练习会话表';

CREATE TABLE IF NOT EXISTS `sys_practice_session_question` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '练习题目ID',
    `session_id` bigint NOT NULL COMMENT '练习会话ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `wrong_question_id` bigint DEFAULT NULL COMMENT '错题ID',
    `question_source` varchar(30) NOT NULL DEFAULT 'WRONG_QUESTION' COMMENT '题目来源：WRONG_QUESTION错题本，QUESTION_BANK题库',
    `bank_question_id` bigint DEFAULT NULL COMMENT '题库题目ID',
    `sort_no` int unsigned NOT NULL DEFAULT 1 COMMENT '题目序号',
    `question_title_snapshot` varchar(255) NOT NULL DEFAULT '' COMMENT '题目标题快照',
    `subject` varchar(50) DEFAULT NULL COMMENT '科目字典键值快照',
    `subject_name` varchar(100) NOT NULL DEFAULT '' COMMENT '科目名称快照',
    `learning_point` varchar(255) DEFAULT NULL COMMENT '知识点快照',
    `difficulty` tinyint DEFAULT NULL COMMENT '难度快照',
    `student_answer` text COMMENT '学生答案',
    `is_correct` tinyint DEFAULT NULL COMMENT '是否正确：0错误，1正确',
    `duration_seconds` int unsigned DEFAULT NULL COMMENT '作答用时，秒',
    `answer_time` datetime DEFAULT NULL COMMENT '作答时间',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_practice_question_session` (`session_id`, `sort_no`),
    KEY `idx_practice_question_user` (`user_id`, `answer_time`),
    KEY `idx_practice_question_wrong` (`wrong_question_id`),
    KEY `idx_practice_question_bank` (`bank_question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='专项练习题目表';

CREATE TABLE IF NOT EXISTS `sys_practice_answer_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '作答记录ID',
    `session_id` bigint NOT NULL COMMENT '练习会话ID',
    `session_question_id` bigint NOT NULL COMMENT '练习题目ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `wrong_question_id` bigint DEFAULT NULL COMMENT '错题ID',
    `question_source` varchar(30) NOT NULL DEFAULT 'WRONG_QUESTION' COMMENT '题目来源：WRONG_QUESTION错题本，QUESTION_BANK题库',
    `bank_question_id` bigint DEFAULT NULL COMMENT '题库题目ID',
    `student_answer` text COMMENT '学生答案',
    `is_correct` tinyint NOT NULL COMMENT '是否正确：0错误，1正确',
    `judge_type` tinyint NOT NULL COMMENT '判题方式：0自评，1自动',
    `duration_seconds` int unsigned NOT NULL DEFAULT 0 COMMENT '作答用时，秒',
    `answer_time` datetime NOT NULL COMMENT '作答时间',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_practice_answer_session` (`session_id`, `answer_time`),
    KEY `idx_practice_answer_user` (`user_id`, `answer_time`),
    KEY `idx_practice_answer_question` (`session_question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='专项练习作答记录表';

SET @review_resource_id = (SELECT `id` FROM `sys_resource` WHERE `code` = 'system:review' LIMIT 1);

INSERT IGNORE INTO `sys_resource` (`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES
('创建专项练习会话', 'system:review:createPracticeSession', @review_resource_id, 110, NOW()),
('专项练习详情', 'system:review:practiceSessionDetail', @review_resource_id, 111, NOW()),
('提交专项练习作答', 'system:review:submitPracticeAnswer', @review_resource_id, 112, NOW()),
('完成专项练习', 'system:review:finishPracticeSession', @review_resource_id, 113, NOW()),
('专项练习历史分页列表', 'system:review:practiceSessionPageList', @review_resource_id, 114, NOW());

SET @review_menu_id = (SELECT `id` FROM `sys_menu` WHERE `code` = 'intelligentReview' LIMIT 1);
SET @practice_create_resource_id = (SELECT `id` FROM `sys_resource` WHERE `code` = 'system:review:createPracticeSession' LIMIT 1);
SET @practice_detail_resource_id = (SELECT `id` FROM `sys_resource` WHERE `code` = 'system:review:practiceSessionDetail' LIMIT 1);
SET @practice_submit_resource_id = (SELECT `id` FROM `sys_resource` WHERE `code` = 'system:review:submitPracticeAnswer' LIMIT 1);
SET @practice_finish_resource_id = (SELECT `id` FROM `sys_resource` WHERE `code` = 'system:review:finishPracticeSession' LIMIT 1);
SET @practice_list_resource_id = (SELECT `id` FROM `sys_resource` WHERE `code` = 'system:review:practiceSessionPageList' LIMIT 1);

INSERT IGNORE INTO `sys_menu`
(`menu_name`, `code`, `pid`, `level`, `sort`, `resource_ids`, `resource_level`, `create_time`)
VALUES ('专项练习', 'intelligentReview-practice', @review_menu_id, 2, 3, '', '', NOW());

SET @practice_menu_id = (SELECT `id` FROM `sys_menu` WHERE `code` = 'intelligentReview-practice' LIMIT 1);
SET @practice_resource_ids = CONCAT_WS(',', @practice_create_resource_id, @practice_detail_resource_id,
    @practice_submit_resource_id, @practice_finish_resource_id, @practice_list_resource_id);
SET @practice_resource_level = CONCAT_WS(',', CONCAT(@review_resource_id, '-', @practice_create_resource_id),
    CONCAT(@review_resource_id, '-', @practice_detail_resource_id),
    CONCAT(@review_resource_id, '-', @practice_submit_resource_id),
    CONCAT(@review_resource_id, '-', @practice_finish_resource_id),
    CONCAT(@review_resource_id, '-', @practice_list_resource_id));

UPDATE `sys_menu`
SET `resource_ids` = @practice_resource_ids,
    `resource_level` = @practice_resource_level
WHERE `id` = @practice_menu_id;

INSERT IGNORE INTO `sys_menu_resource` (`menu_id`, `resource_id`, `resource_level`, `create_time`)
VALUES
(@practice_menu_id, @practice_create_resource_id, CONCAT(@review_resource_id, '-', @practice_create_resource_id), NOW()),
(@practice_menu_id, @practice_detail_resource_id, CONCAT(@review_resource_id, '-', @practice_detail_resource_id), NOW()),
(@practice_menu_id, @practice_submit_resource_id, CONCAT(@review_resource_id, '-', @practice_submit_resource_id), NOW()),
(@practice_menu_id, @practice_finish_resource_id, CONCAT(@review_resource_id, '-', @practice_finish_resource_id), NOW()),
(@practice_menu_id, @practice_list_resource_id, CONCAT(@review_resource_id, '-', @practice_list_resource_id), NOW());

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT `role_id`, @practice_menu_id, NOW()
FROM `sys_role_menu`
WHERE `menu_id` = @review_menu_id;
