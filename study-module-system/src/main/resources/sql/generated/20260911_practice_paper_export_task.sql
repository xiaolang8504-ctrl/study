-- P1：服务端练习卷 PDF/DOCX 异步导出任务及下载历史。
CREATE TABLE IF NOT EXISTS `sys_practice_paper_export_task` (
 `id` bigint NOT NULL AUTO_INCREMENT, `user_id` bigint NOT NULL, `session_id` bigint NOT NULL,
 `format` varchar(8) NOT NULL, `answer_mode` tinyint NOT NULL, `status` tinyint NOT NULL,
 `file_id` int DEFAULT NULL, `file_name` varchar(255) DEFAULT NULL, `error_message` varchar(500) DEFAULT NULL,
 `finish_time` datetime DEFAULT NULL, `create_time` datetime NOT NULL, `update_time` datetime NOT NULL,
 PRIMARY KEY (`id`), KEY `idx_practice_export_user_time` (`user_id`,`create_time`), KEY `idx_practice_export_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专项练习卷导出任务与历史';
