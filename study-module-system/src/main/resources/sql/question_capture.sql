CREATE TABLE IF NOT EXISTS `sys_question_capture_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `grade` varchar(32) NOT NULL, `subject` varchar(32) NOT NULL, `question_type` varchar(32) NOT NULL, `source` varchar(32) NOT NULL,
  `learning_point` varchar(500) DEFAULT NULL, `error_labels` varchar(500) DEFAULT NULL, `client_request_id` varchar(64) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待处理、1处理中、2待确认、3已完成、4失败',
  `fail_reason` varchar(500) DEFAULT NULL, `retry_count` int NOT NULL DEFAULT 0, `create_id` bigint NOT NULL, `create_time` datetime NOT NULL, `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_capture_task_user_request` (`create_id`,`client_request_id`), KEY `idx_capture_task_user_time` (`create_id`,`create_time`), KEY `idx_capture_task_user_status` (`create_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目采集任务';
CREATE TABLE IF NOT EXISTS `sys_question_capture_page` (
  `id` bigint NOT NULL AUTO_INCREMENT, `task_id` bigint NOT NULL, `image_file_id` bigint NOT NULL, `cleaned_file_id` bigint DEFAULT NULL, `source_file_id` bigint DEFAULT NULL, `source_page_no` int DEFAULT NULL, `page_no` int NOT NULL,
  `status` tinyint NOT NULL DEFAULT 0, `fail_reason` varchar(500) DEFAULT NULL, `retry_count` int NOT NULL DEFAULT 0, `clean_status` tinyint NOT NULL DEFAULT 0, `clean_fail_reason` varchar(500) DEFAULT NULL, `create_time` datetime NOT NULL, `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_capture_page_task_no` (`task_id`,`page_no`), KEY `idx_capture_page_task` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目采集页面';
CREATE TABLE IF NOT EXISTS `sys_question_capture_region` (
  `id` bigint NOT NULL AUTO_INCREMENT, `task_id` bigint NOT NULL, `page_id` bigint NOT NULL, `region_no` int NOT NULL,
  `confidence` int NOT NULL DEFAULT 100, `question_title_confidence` int NOT NULL DEFAULT 0, `question_content_confidence` int NOT NULL DEFAULT 0, `wrong_answer_confidence` int NOT NULL DEFAULT 0, `correct_answer_confidence` int NOT NULL DEFAULT 0, `analysis_confidence` int NOT NULL DEFAULT 0, `manually_corrected` tinyint NOT NULL DEFAULT 0, `wrong_question_id` bigint DEFAULT NULL, `left_position` int NOT NULL DEFAULT 0, `top_position` int NOT NULL DEFAULT 0, `width` int NOT NULL DEFAULT 10000, `height` int NOT NULL DEFAULT 10000, `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待确认、1已确认、2跳过',
  `question_title` varchar(500) DEFAULT NULL, `question_content` longtext, `wrong_answer` longtext, `correct_answer` longtext, `wrong_reason` longtext, `analysis` longtext,
  `grade` varchar(32) DEFAULT NULL, `subject` varchar(32) DEFAULT NULL, `question_type` varchar(32) DEFAULT NULL, `source` varchar(32) DEFAULT NULL, `learning_point` varchar(500) DEFAULT NULL, `error_labels` varchar(500) DEFAULT NULL,
  `create_time` datetime NOT NULL, `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_capture_region_page_no` (`page_id`,`region_no`), UNIQUE KEY `uk_capture_region_wrong_question` (`wrong_question_id`), KEY `idx_capture_region_task_status` (`task_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目采集题块';
