-- 智能复习基础模块
-- 适用数据库：MySQL 8.0+

CREATE TABLE IF NOT EXISTS `sys_review_plan` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '复习计划ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `plan_name` varchar(50) NOT NULL DEFAULT '我的错题复习' COMMENT '计划名称',
    `daily_limit` smallint unsigned NOT NULL DEFAULT 20 COMMENT '每日复习题量上限',
    `reminder_enabled` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '是否开启提醒: 0否, 1是',
    `reminder_time` time NOT NULL DEFAULT '19:30:00' COMMENT '每日提醒时间',
    `review_week_days` varchar(20) NOT NULL DEFAULT '1,2,3,4,5,6,7' COMMENT '复习星期: 1周一至7周日，逗号分隔',
    `algorithm_version` varchar(30) NOT NULL DEFAULT 'stage-v1' COMMENT '排期算法版本',
    `status` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '计划状态: 0停用, 1启用',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_review_plan_user` (`user_id`),
    KEY `idx_review_plan_reminder` (`status`, `reminder_enabled`, `reminder_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户错题复习计划表';

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

CREATE TABLE IF NOT EXISTS `sys_review_item` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '复习项目ID',
    `plan_id` bigint NOT NULL COMMENT '复习计划ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `wrong_question_id` bigint NOT NULL COMMENT '错题ID',
    `item_status` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '项目状态: 0正常, 1暂停, 2结束',
    `stage` tinyint unsigned NOT NULL DEFAULT 1 COMMENT '当前复习阶段: 0-7',
    `current_interval_minutes` int unsigned NOT NULL DEFAULT 1440 COMMENT '当前复习间隔，单位分钟',
    `last_feedback` tinyint DEFAULT NULL COMMENT '最近反馈: 0忘记, 1困难, 2掌握, 3很简单',
    `mastery_score` tinyint unsigned NOT NULL DEFAULT 40 COMMENT '当前掌握度，0-100',
    `correct_streak` smallint unsigned NOT NULL DEFAULT 0 COMMENT '连续掌握次数',
    `wrong_streak` smallint unsigned NOT NULL DEFAULT 0 COMMENT '连续错误次数',
    `lapse_count` smallint unsigned NOT NULL DEFAULT 0 COMMENT '遗忘次数',
    `review_count` int unsigned NOT NULL DEFAULT 0 COMMENT '累计复习次数',
    `last_review_time` datetime DEFAULT NULL COMMENT '最近复习时间',
    `next_review_time` datetime NOT NULL COMMENT '下次复习时间',
    `mastered_time` datetime DEFAULT NULL COMMENT '达到掌握状态时间',
    `algorithm_version` varchar(30) NOT NULL DEFAULT 'stage-v1' COMMENT '当前算法版本',
    `version` int unsigned NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_review_item_user_question` (`user_id`, `wrong_question_id`),
    KEY `idx_review_item_user_due` (`user_id`, `item_status`, `next_review_time`),
    KEY `idx_review_item_plan_due` (`plan_id`, `item_status`, `next_review_time`),
    KEY `idx_review_item_mastery` (`user_id`, `item_status`, `mastery_score`, `next_review_time`),
    KEY `idx_review_item_question` (`wrong_question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题复习排期表';

CREATE TABLE IF NOT EXISTS `sys_review_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '复习记录ID',
    `request_id` varchar(64) NOT NULL COMMENT '客户端幂等请求ID',
    `plan_id` bigint NOT NULL COMMENT '复习计划ID',
    `review_item_id` bigint NOT NULL COMMENT '复习项目ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `wrong_question_id` bigint NOT NULL COMMENT '错题ID',
    `question_title_snapshot` varchar(255) NOT NULL DEFAULT '' COMMENT '复习时题目标题快照',
    `subject` varchar(50) NOT NULL DEFAULT '' COMMENT '复习时科目字典键值快照',
    `subject_name_snapshot` varchar(100) NOT NULL DEFAULT '' COMMENT '复习时科目名称快照',
    `scheduled_time` datetime NOT NULL COMMENT '本次原计划复习时间',
    `start_time` datetime NOT NULL COMMENT '开始查看题目时间',
    `reveal_time` datetime DEFAULT NULL COMMENT '查看答案时间',
    `review_time` datetime NOT NULL COMMENT '提交反馈时间',
    `feedback` tinyint unsigned NOT NULL COMMENT '反馈: 0忘记, 1困难, 2掌握, 3很简单',
    `answer_duration_seconds` int unsigned NOT NULL DEFAULT 0 COMMENT '主动回忆用时，秒',
    `student_answer` text COMMENT '学生本次答案快照',
    `is_correct` tinyint unsigned DEFAULT NULL COMMENT '本次作答是否正确: 0错误, 1正确',
    `answer_judge_type` tinyint unsigned DEFAULT NULL COMMENT '判定来源: 0学生自评, 1系统自动判定',
    `is_overdue` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '是否逾期: 0否, 1是',
    `stage_before` tinyint unsigned NOT NULL COMMENT '复习前阶段',
    `stage_after` tinyint unsigned NOT NULL COMMENT '复习后阶段',
    `mastery_score_before` tinyint unsigned DEFAULT NULL COMMENT '复习前掌握度，0-100',
    `mastery_score_after` tinyint unsigned DEFAULT NULL COMMENT '复习后掌握度，0-100',
    `mastery_score_delta` smallint DEFAULT NULL COMMENT '本次掌握度变化',
    `correct_streak_after` smallint unsigned DEFAULT NULL COMMENT '反馈后连续正确次数',
    `wrong_streak_after` smallint unsigned DEFAULT NULL COMMENT '反馈后连续错误次数',
    `interval_before_minutes` int unsigned NOT NULL COMMENT '复习前间隔，分钟',
    `interval_after_minutes` int unsigned NOT NULL COMMENT '复习后间隔，分钟',
    `next_review_time` datetime NOT NULL COMMENT '计算后的下次复习时间',
    `algorithm_version` varchar(30) NOT NULL COMMENT '计算使用的算法版本',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_review_record_request` (`request_id`),
    KEY `idx_review_record_user_time` (`user_id`, `review_time`),
    KEY `idx_review_record_user_subject_time` (`user_id`, `subject`, `review_time`),
    KEY `idx_review_record_item_time` (`review_item_id`, `review_time`),
    KEY `idx_review_record_question_time` (`wrong_question_id`, `review_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题复习历史记录表';

CREATE TABLE IF NOT EXISTS `sys_review_reminder` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提醒任务ID',
    `plan_id` bigint NOT NULL COMMENT '复习计划ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `reminder_date` date NOT NULL COMMENT '提醒日期',
    `reminder_type` varchar(30) NOT NULL DEFAULT 'dailyReview' COMMENT '提醒类型',
    `due_count` int unsigned NOT NULL DEFAULT 0 COMMENT '今日到期数量',
    `overdue_count` int unsigned NOT NULL DEFAULT 0 COMMENT '逾期数量',
    `subject_summary` varchar(500) NOT NULL DEFAULT '' COMMENT '各科任务数量摘要',
    `scheduled_time` datetime NOT NULL COMMENT '计划发送时间',
    `send_status` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '发送状态: 0待发送, 1已发送, 2发送失败',
    `retry_count` tinyint unsigned NOT NULL DEFAULT 0 COMMENT '重试次数',
    `msg_id` bigint DEFAULT NULL COMMENT '生成的站内消息ID',
    `failure_reason` varchar(500) NOT NULL DEFAULT '' COMMENT '失败原因',
    `sent_time` datetime DEFAULT NULL COMMENT '实际发送时间',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_review_reminder_user_date_type` (`user_id`, `reminder_date`, `reminder_type`),
    KEY `idx_review_reminder_send` (`send_status`, `scheduled_time`, `retry_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题复习提醒任务表';

-- 智能复习首页菜单与权限资源（可重复执行）
INSERT IGNORE INTO `sys_menu`
(`menu_name`, `code`, `pid`, `level`, `sort`, `resource_ids`, `resource_level`, `create_time`)
VALUES ('智能复习', 'intelligentReview', 0, 1, 4, '', '', NOW());

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('智能复习', 'system:review', 0, 83, NOW());

SET @review_resource_id = (SELECT `id` FROM `sys_resource` WHERE `code` = 'system:review' LIMIT 1);

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('初始化智能复习首页', 'system:review:initializeReviewHome', @review_resource_id, 84, NOW());

SET @review_menu_id = (SELECT `id` FROM `sys_menu` WHERE `code` = 'intelligentReview' LIMIT 1);
SET @review_home_resource_id = (SELECT `id` FROM `sys_resource`
                                WHERE `code` = 'system:review:initializeReviewHome' LIMIT 1);
SET @review_resource_level = CONCAT(@review_resource_id, '-', @review_home_resource_id);

UPDATE `sys_menu`
SET `resource_ids` = CAST(@review_home_resource_id AS CHAR),
    `resource_level` = @review_resource_level
WHERE `id` = @review_menu_id;

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
VALUES (@review_menu_id, @review_home_resource_id, @review_resource_level, NOW());

-- 智能复习子菜单：复习概览、今日复习
INSERT IGNORE INTO `sys_menu`
(`menu_name`, `code`, `pid`, `level`, `sort`, `resource_ids`, `resource_level`, `create_time`)
VALUES ('复习概览', 'intelligentReview-home', @review_menu_id, 2, 1,
        CAST(@review_home_resource_id AS CHAR), @review_resource_level, NOW());

INSERT IGNORE INTO `sys_menu`
(`menu_name`, `code`, `pid`, `level`, `sort`, `resource_ids`, `resource_level`, `create_time`)
VALUES ('今日复习', 'intelligentReview-today', @review_menu_id, 2, 2, '', '', NOW());

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('今日复习首页', 'system:review:todayReviewHome', @review_resource_id, 85, NOW());

SET @review_overview_menu_id = (SELECT `id` FROM `sys_menu`
                                WHERE `code` = 'intelligentReview-home' LIMIT 1);
SET @review_today_menu_id = (SELECT `id` FROM `sys_menu`
                             WHERE `code` = 'intelligentReview-today' LIMIT 1);
SET @review_today_resource_id = (SELECT `id` FROM `sys_resource`
                                 WHERE `code` = 'system:review:todayReviewHome' LIMIT 1);
SET @review_today_resource_level = CONCAT(@review_resource_id, '-', @review_today_resource_id);

UPDATE `sys_menu`
SET `resource_ids` = '', `resource_level` = ''
WHERE `id` = @review_menu_id;

UPDATE `sys_menu`
SET `resource_ids` = CAST(@review_home_resource_id AS CHAR),
    `resource_level` = @review_resource_level
WHERE `id` = @review_overview_menu_id;

UPDATE `sys_menu`
SET `resource_ids` = CAST(@review_today_resource_id AS CHAR),
    `resource_level` = @review_today_resource_level
WHERE `id` = @review_today_menu_id;

DELETE FROM `sys_menu_resource`
WHERE `menu_id` = @review_menu_id AND `resource_id` = @review_home_resource_id;

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
VALUES (@review_overview_menu_id, @review_home_resource_id, @review_resource_level, NOW());

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
VALUES (@review_today_menu_id, @review_today_resource_id, @review_today_resource_level, NOW());

-- 已经拥有智能复习父菜单的普通角色自动继承两个子菜单
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT `role_id`, @review_overview_menu_id, NOW()
FROM `sys_role_menu`
WHERE `menu_id` = @review_menu_id;

-- 复习历史与站内提醒菜单、API资源（可重复执行）
INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('复习历史分页列表', 'system:review:reviewHistoryPageList', @review_resource_id, 90, NOW());

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('复习提醒分页列表', 'system:review:reviewReminderPageList', @review_resource_id, 91, NOW());

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('复习提醒设为已读', 'system:review:readReviewReminder', @review_resource_id, 92, NOW());

SET @review_history_resource_id = (SELECT `id` FROM `sys_resource`
                                   WHERE `code` = 'system:review:reviewHistoryPageList' LIMIT 1);
SET @review_reminder_list_resource_id = (SELECT `id` FROM `sys_resource`
                                         WHERE `code` = 'system:review:reviewReminderPageList' LIMIT 1);
SET @review_reminder_read_resource_id = (SELECT `id` FROM `sys_resource`
                                         WHERE `code` = 'system:review:readReviewReminder' LIMIT 1);
SET @review_history_resource_level = CONCAT(@review_resource_id, '-', @review_history_resource_id);
SET @review_reminder_list_resource_level = CONCAT(@review_resource_id, '-',
                                                  @review_reminder_list_resource_id);
SET @review_reminder_read_resource_level = CONCAT(@review_resource_id, '-',
                                                  @review_reminder_read_resource_id);

INSERT IGNORE INTO `sys_menu`
(`menu_name`, `code`, `pid`, `level`, `sort`, `resource_ids`, `resource_level`, `create_time`)
VALUES ('复习历史', 'intelligentReview-history', @review_menu_id, 2, 4,
        CONCAT_WS(',', @review_history_resource_id, @review_reminder_list_resource_id,
                  @review_reminder_read_resource_id),
        CONCAT_WS(',', @review_history_resource_level, @review_reminder_list_resource_level,
                  @review_reminder_read_resource_level), NOW());

SET @review_history_menu_id = (SELECT `id` FROM `sys_menu`
                               WHERE `code` = 'intelligentReview-history' LIMIT 1);

UPDATE `sys_menu`
SET `resource_ids` = CONCAT_WS(',', @review_history_resource_id,
                              @review_reminder_list_resource_id, @review_reminder_read_resource_id),
    `resource_level` = CONCAT_WS(',', @review_history_resource_level,
                                 @review_reminder_list_resource_level,
                                 @review_reminder_read_resource_level)
WHERE `id` = @review_history_menu_id;

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
VALUES (@review_history_menu_id, @review_history_resource_id,
        @review_history_resource_level, NOW());

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
VALUES (@review_history_menu_id, @review_reminder_list_resource_id,
        @review_reminder_list_resource_level, NOW());

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
VALUES (@review_history_menu_id, @review_reminder_read_resource_id,
        @review_reminder_read_resource_level, NOW());

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT `role_id`, @review_history_menu_id, NOW()
FROM `sys_role_menu`
WHERE `menu_id` = @review_menu_id;

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT `role_id`, @review_today_menu_id, NOW()
FROM `sys_role_menu`
WHERE `menu_id` = @review_menu_id;

-- 查看答案与四级反馈API资源
INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('查看复习答案', 'system:review:reviewAnswer', @review_resource_id, 86, NOW());

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('提交四级反馈', 'system:review:submitReviewFeedback', @review_resource_id, 87, NOW());

SET @review_answer_resource_id = (SELECT `id` FROM `sys_resource`
                                  WHERE `code` = 'system:review:reviewAnswer' LIMIT 1);
SET @review_feedback_resource_id = (SELECT `id` FROM `sys_resource`
                                    WHERE `code` = 'system:review:submitReviewFeedback' LIMIT 1);
SET @review_answer_resource_level = CONCAT(@review_resource_id, '-', @review_answer_resource_id);
SET @review_feedback_resource_level = CONCAT(@review_resource_id, '-', @review_feedback_resource_id);

UPDATE `sys_menu`
SET `resource_ids` = CONCAT_WS(',', @review_today_resource_id,
                              @review_answer_resource_id, @review_feedback_resource_id),
    `resource_level` = CONCAT_WS(',', @review_today_resource_level,
                                 @review_answer_resource_level, @review_feedback_resource_level)
WHERE `id` = @review_today_menu_id;

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
VALUES (@review_today_menu_id, @review_answer_resource_id,
        @review_answer_resource_level, NOW());

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
VALUES (@review_today_menu_id, @review_feedback_resource_id,
        @review_feedback_resource_level, NOW());

-- 复习计划设置菜单与API资源（可重复执行）
INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('查询复习计划设置', 'system:review:reviewPlanSetting', @review_resource_id, 88, NOW());

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('更新复习计划设置', 'system:review:updateReviewPlanSetting', @review_resource_id, 89, NOW());

SET @review_setting_query_resource_id = (SELECT `id` FROM `sys_resource`
                                         WHERE `code` = 'system:review:reviewPlanSetting' LIMIT 1);
SET @review_setting_update_resource_id = (SELECT `id` FROM `sys_resource`
                                          WHERE `code` = 'system:review:updateReviewPlanSetting' LIMIT 1);
SET @review_setting_query_resource_level = CONCAT(@review_resource_id, '-',
                                                  @review_setting_query_resource_id);
SET @review_setting_update_resource_level = CONCAT(@review_resource_id, '-',
                                                   @review_setting_update_resource_id);

INSERT IGNORE INTO `sys_menu`
(`menu_name`, `code`, `pid`, `level`, `sort`, `resource_ids`, `resource_level`, `create_time`)
VALUES ('计划设置', 'intelligentReview-setting', @review_menu_id, 2, 3,
        CONCAT_WS(',', @review_setting_query_resource_id, @review_setting_update_resource_id),
        CONCAT_WS(',', @review_setting_query_resource_level, @review_setting_update_resource_level), NOW());

SET @review_setting_menu_id = (SELECT `id` FROM `sys_menu`
                               WHERE `code` = 'intelligentReview-setting' LIMIT 1);

UPDATE `sys_menu`
SET `resource_ids` = CONCAT_WS(',', @review_setting_query_resource_id,
                              @review_setting_update_resource_id),
    `resource_level` = CONCAT_WS(',', @review_setting_query_resource_level,
                                 @review_setting_update_resource_level)
WHERE `id` = @review_setting_menu_id;

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
VALUES (@review_setting_menu_id, @review_setting_query_resource_id,
        @review_setting_query_resource_level, NOW());

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
VALUES (@review_setting_menu_id, @review_setting_update_resource_id,
        @review_setting_update_resource_level, NOW());

-- 已经拥有智能复习父菜单的普通角色自动继承计划设置菜单
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT `role_id`, @review_setting_menu_id, NOW()
FROM `sys_role_menu`
WHERE `menu_id` = @review_menu_id;
