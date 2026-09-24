-- P0-5～P0-6：错题整理效率和可回退的真实卷面清理。

ALTER TABLE `sys_wrong_question`
    ADD COLUMN `textbook_version` varchar(100) DEFAULT NULL COMMENT '教材版本' AFTER `capture_height`,
    ADD COLUMN `chapter_name` varchar(150) DEFAULT NULL COMMENT '教材章节' AFTER `textbook_version`,
    ADD COLUMN `favorite` tinyint NOT NULL DEFAULT 0 COMMENT '是否收藏：0否、1是' AFTER `chapter_name`,
    ADD COLUMN `priority_level` tinyint NOT NULL DEFAULT 0 COMMENT '整理优先级：0普通、1-5提高' AFTER `favorite`,
    ADD COLUMN `capture_original_file_id` bigint DEFAULT NULL COMMENT '采集页面原图文件ID' AFTER `capture_height`,
    ADD COLUMN `capture_cleaned_file_id` bigint DEFAULT NULL COMMENT '确认时可用的清理页文件ID' AFTER `capture_original_file_id`,
    ADD COLUMN `capture_image_mode` varchar(16) NOT NULL DEFAULT 'ORIGINAL' COMMENT '题块裁剪来源：ORIGINAL、CLEANED' AFTER `capture_cleaned_file_id`,
    ADD KEY `idx_wrong_question_organize_filter` (`create_id`, `favorite`, `priority_level`, `status`),
    ADD KEY `idx_wrong_question_textbook_chapter` (`create_id`, `textbook_version`, `chapter_name`);

CREATE TABLE IF NOT EXISTS `sys_wrong_question_tag` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '学生ID',
    `tag_name` varchar(30) NOT NULL COMMENT '个人标签',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wrong_question_tag_user_name` (`user_id`, `tag_name`),
    KEY `idx_wrong_question_tag_user` (`user_id`, `tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题个人标签';

CREATE TABLE IF NOT EXISTS `sys_wrong_question_tag_relation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `wrong_question_id` bigint NOT NULL COMMENT '错题ID',
    `tag_id` bigint NOT NULL COMMENT '个人标签ID',
    `user_id` bigint NOT NULL COMMENT '学生ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wrong_question_tag_relation` (`wrong_question_id`, `tag_id`),
    KEY `idx_wrong_question_tag_relation_user_tag` (`user_id`, `tag_id`, `wrong_question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题个人标签关系';

CREATE TABLE IF NOT EXISTS `sys_wrong_question_saved_filter` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL COMMENT '学生ID',
    `filter_name` varchar(50) NOT NULL COMMENT '筛选名称',
    `filter_json` varchar(4000) NOT NULL COMMENT '筛选条件JSON',
    `sort_no` int NOT NULL DEFAULT 0,
    `default_flag` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认：0否、1是',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wrong_question_saved_filter` (`user_id`, `filter_name`),
    KEY `idx_wrong_question_saved_filter_user_sort` (`user_id`, `sort_no`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题常用筛选';

-- 旧 cleaned_file_id 实际为灰度预览，迁移后不再将它当作去笔迹结果。
ALTER TABLE `sys_question_capture_page`
    ADD COLUMN `grayscale_file_id` bigint DEFAULT NULL COMMENT '灰度预览文件ID，不代表已去笔迹' AFTER `cleaned_file_id`,
    ADD COLUMN `grayscale_status` tinyint NOT NULL DEFAULT 0 COMMENT '灰度预览状态：0未生成、1处理中、2完成、4失败' AFTER `clean_status`,
    ADD COLUMN `grayscale_fail_reason` varchar(500) DEFAULT NULL COMMENT '灰度预览失败原因' AFTER `grayscale_status`,
    ADD COLUMN `clean_provider` varchar(50) DEFAULT NULL COMMENT '去笔迹提供方' AFTER `clean_fail_reason`,
    ADD COLUMN `clean_algorithm_version` varchar(100) DEFAULT NULL COMMENT '去笔迹算法版本' AFTER `clean_provider`,
    ADD COLUMN `clean_quality_score` int DEFAULT NULL COMMENT '去除像素比例，万分比' AFTER `clean_algorithm_version`,
    ADD COLUMN `clean_requested_time` datetime DEFAULT NULL COMMENT '去笔迹请求时间' AFTER `clean_quality_score`,
    ADD COLUMN `clean_finished_time` datetime DEFAULT NULL COMMENT '去笔迹完成时间' AFTER `clean_requested_time`;

UPDATE `sys_question_capture_page`
SET `grayscale_file_id` = `cleaned_file_id`,
    `grayscale_status` = `clean_status`,
    `grayscale_fail_reason` = `clean_fail_reason`,
    `cleaned_file_id` = NULL,
    `clean_status` = 0,
    `clean_fail_reason` = NULL
WHERE `cleaned_file_id` IS NOT NULL OR `clean_status` <> 0;

ALTER TABLE `sys_question_capture_region`
    ADD COLUMN `clean_image_mode` varchar(16) NOT NULL DEFAULT 'ORIGINAL' COMMENT '题块确认裁剪图：ORIGINAL、CLEANED' AFTER `error_labels`;

CREATE TABLE IF NOT EXISTS `sys_question_capture_clean_version` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `page_id` bigint NOT NULL COMMENT '采集页面ID',
    `task_id` bigint NOT NULL COMMENT '采集任务ID',
    `user_id` bigint NOT NULL COMMENT '学生ID',
    `original_file_id` bigint NOT NULL COMMENT '原图文件ID',
    `cleaned_file_id` bigint DEFAULT NULL COMMENT '清理图文件ID',
    `provider` varchar(50) NOT NULL COMMENT '处理提供方',
    `algorithm_version` varchar(100) NOT NULL COMMENT '算法版本',
    `status` tinyint NOT NULL COMMENT '0待处理、1处理中、2完成、4失败',
    `quality_score` int DEFAULT NULL COMMENT '去除像素比例，万分比',
    `fail_reason` varchar(500) DEFAULT NULL,
    `active_flag` tinyint NOT NULL DEFAULT 0 COMMENT '当前生效版本：0否、1是',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_capture_clean_version_page` (`page_id`, `active_flag`, `id`),
    KEY `idx_capture_clean_version_user` (`user_id`, `task_id`, `page_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='卷面清理版本';
