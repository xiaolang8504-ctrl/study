-- P0-1～P0-4：题目素材、可复现练习卷、订正草稿与重复来源归并。
ALTER TABLE `sys_wrong_question`
    ADD COLUMN `content_format` varchar(20) NOT NULL DEFAULT 'TEXT'
        COMMENT '内容格式：TEXT、LATEX、RICH_TEXT' AFTER `question_content`,
    ADD COLUMN `options_json` text DEFAULT NULL
        COMMENT '结构化选项JSON' AFTER `content_format`,
    ADD COLUMN `question_fingerprint` char(64) DEFAULT NULL
        COMMENT '规范化题干SHA-256指纹' AFTER `options_json`,
    ADD COLUMN `merged_to_id` bigint DEFAULT NULL
        COMMENT '合并后的主错题ID，空表示当前有效题' AFTER `question_fingerprint`,
    ADD KEY `idx_wrong_question_owner_fingerprint` (`create_id`, `question_fingerprint`),
    ADD KEY `idx_wrong_question_merged_to` (`merged_to_id`);

UPDATE `sys_wrong_question`
SET `question_fingerprint` = SHA2(LOWER(REPLACE(REPLACE(TRIM(CONCAT_WS(' ', `question_title`, `question_content`, `options_json`)), ' ', ''), '\n', '')), 256)
WHERE `question_fingerprint` IS NULL;

CREATE TABLE IF NOT EXISTS `sys_wrong_question_asset` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '素材ID',
    `wrong_question_id` bigint NOT NULL COMMENT '错题ID',
    `user_id` bigint NOT NULL COMMENT '所属学生ID',
    `asset_type` varchar(32) NOT NULL COMMENT 'SOURCE_PAGE、QUESTION_CROP、QUESTION_IMAGE、OPTION_IMAGE、CONTENT_IMAGE',
    `file_id` int DEFAULT NULL COMMENT '文件服务ID',
    `upload_type` varchar(64) DEFAULT NULL COMMENT '文件上传类型',
    `image_url` varchar(1000) DEFAULT NULL COMMENT '兼容外部图片地址',
    `label` varchar(100) DEFAULT NULL COMMENT '素材标签',
    `sort_no` int NOT NULL DEFAULT 1 COMMENT '素材顺序',
    `left_position` int DEFAULT NULL COMMENT '归一化裁剪左坐标',
    `top_position` int DEFAULT NULL COMMENT '归一化裁剪上坐标',
    `width` int DEFAULT NULL COMMENT '归一化裁剪宽度',
    `height` int DEFAULT NULL COMMENT '归一化裁剪高度',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_wrong_question_asset_question` (`wrong_question_id`, `sort_no`, `id`),
    KEY `idx_wrong_question_asset_user` (`user_id`, `wrong_question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题有序素材';

INSERT INTO `sys_wrong_question_asset`
(`wrong_question_id`, `user_id`, `asset_type`, `file_id`, `upload_type`, `label`, `sort_no`,
 `left_position`, `top_position`, `width`, `height`, `create_time`, `update_time`)
SELECT `id`, `create_id`, 'QUESTION_CROP', CAST(`image_url` AS UNSIGNED), 'wrongQuestion', '采集题块', 1,
       `capture_left_position`, `capture_top_position`, `capture_width`, `capture_height`, NOW(), NOW()
FROM `sys_wrong_question`
WHERE `capture_page_id` IS NOT NULL AND `image_url` REGEXP '^[0-9]+$';

CREATE TABLE IF NOT EXISTS `sys_wrong_question_correction_draft` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `wrong_question_id` bigint NOT NULL,
    `user_id` bigint NOT NULL,
    `thinking` text DEFAULT NULL COMMENT '独立思路',
    `error_reason` text DEFAULT NULL COMMENT '学生自述错因',
    `correction_answer` text DEFAULT NULL,
    `correction_analysis` text DEFAULT NULL,
    `correction_image_url` varchar(255) DEFAULT NULL,
    `correction_remark` varchar(500) DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_correction_draft_question_user` (`wrong_question_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题订正草稿';

ALTER TABLE `sys_wrong_question_correction_record`
    ADD COLUMN `revision_no` int NOT NULL DEFAULT 1 COMMENT '订正版本号' AFTER `wrong_question_id`,
    ADD COLUMN `thinking` text DEFAULT NULL COMMENT '提交前独立思路' AFTER `revision_no`,
    ADD COLUMN `error_reason` text DEFAULT NULL COMMENT '学生自述错因' AFTER `thinking`;

UPDATE `sys_wrong_question_correction_record` AS `record`
JOIN (
    SELECT `id`, ROW_NUMBER() OVER (
        PARTITION BY `wrong_question_id` ORDER BY `create_time`, `id`
    ) AS `revision_no`
    FROM `sys_wrong_question_correction_record`
) AS `ranked` ON `ranked`.`id` = `record`.`id`
SET `record`.`revision_no` = `ranked`.`revision_no`;

ALTER TABLE `sys_wrong_question_correction_record`
    ADD UNIQUE KEY `uk_correction_record_revision` (`wrong_question_id`, `revision_no`);

CREATE TABLE IF NOT EXISTS `sys_wrong_question_occurrence` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `canonical_question_id` bigint NOT NULL COMMENT '当前归属主错题',
    `origin_wrong_question_id` bigint NOT NULL COMMENT '原始错题ID',
    `user_id` bigint NOT NULL,
    `wrong_answer` text DEFAULT NULL,
    `source` varchar(50) DEFAULT NULL,
    `source_name` varchar(100) DEFAULT NULL,
    `capture_task_id` bigint DEFAULT NULL,
    `capture_page_id` bigint DEFAULT NULL,
    `capture_region_id` bigint DEFAULT NULL,
    `capture_source_page_no` int DEFAULT NULL,
    `left_position` int DEFAULT NULL,
    `top_position` int DEFAULT NULL,
    `width` int DEFAULT NULL,
    `height` int DEFAULT NULL,
    `occurred_at` datetime DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wrong_question_occurrence_origin` (`origin_wrong_question_id`),
    KEY `idx_wrong_question_occurrence_canonical` (`canonical_question_id`, `occurred_at`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='同题多次错误来源';

INSERT IGNORE INTO `sys_wrong_question_occurrence`
(`canonical_question_id`, `origin_wrong_question_id`, `user_id`, `wrong_answer`, `source`, `source_name`,
 `capture_task_id`, `capture_page_id`, `capture_region_id`, `capture_source_page_no`,
 `left_position`, `top_position`, `width`, `height`, `occurred_at`, `create_time`)
SELECT `id`, `id`, `create_id`, `wrong_answer`, `source`, `source_name`, `capture_task_id`, `capture_page_id`,
       `capture_region_id`, `capture_source_page_no`, `capture_left_position`, `capture_top_position`,
       `capture_width`, `capture_height`, `create_time`, NOW()
FROM `sys_wrong_question`;

CREATE TABLE IF NOT EXISTS `sys_wrong_question_duplicate_relation` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL,
    `left_question_id` bigint NOT NULL,
    `right_question_id` bigint NOT NULL,
    `match_type` varchar(40) NOT NULL COMMENT 'EXACT、SAME_QUESTION_DIFFERENT_SOURCE、SIMILAR',
    `similarity_score` int NOT NULL DEFAULT 0,
    `status` varchar(20) NOT NULL DEFAULT 'CANDIDATE' COMMENT 'CANDIDATE、MERGED、IGNORED、REVERSED',
    `kept_question_id` bigint DEFAULT NULL,
    `merged_question_id` bigint DEFAULT NULL,
    `merged_before_status` tinyint DEFAULT NULL,
    `create_id` bigint NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_wrong_question_duplicate_pair` (`user_id`, `left_question_id`, `right_question_id`),
    KEY `idx_wrong_question_duplicate_status` (`user_id`, `status`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题重复关系';

ALTER TABLE `sys_practice_session`
    ADD COLUMN `paper_version` int NOT NULL DEFAULT 1 COMMENT '练习卷版本' AFTER `update_time`,
    ADD COLUMN `column_count` tinyint NOT NULL DEFAULT 1 COMMENT '栏数：1或2' AFTER `paper_version`;

ALTER TABLE `sys_practice_session_question`
    ADD COLUMN `question_content_snapshot` text DEFAULT NULL COMMENT '题干快照' AFTER `question_title_snapshot`,
    ADD COLUMN `content_format_snapshot` varchar(20) NOT NULL DEFAULT 'TEXT' COMMENT '内容格式快照' AFTER `question_content_snapshot`,
    ADD COLUMN `options_json_snapshot` text DEFAULT NULL COMMENT '选项快照' AFTER `content_format_snapshot`,
    ADD COLUMN `asset_snapshot_json` mediumtext DEFAULT NULL COMMENT '有序素材快照JSON' AFTER `options_json_snapshot`,
    ADD COLUMN `correct_answer_snapshot` text DEFAULT NULL COMMENT '答案快照' AFTER `asset_snapshot_json`,
    ADD COLUMN `analysis_snapshot` text DEFAULT NULL COMMENT '解析快照' AFTER `correct_answer_snapshot`;

ALTER TABLE `sys_practice_paper_export_task`
    ADD COLUMN `paper_version` int NOT NULL DEFAULT 1 COMMENT '导出的练习卷版本' AFTER `session_id`;
