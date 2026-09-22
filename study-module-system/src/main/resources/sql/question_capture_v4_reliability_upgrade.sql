ALTER TABLE `sys_question_capture_task`
    ADD COLUMN `retry_count` int NOT NULL DEFAULT 0 COMMENT 'OCR重试次数';

ALTER TABLE `sys_question_capture_page`
    ADD COLUMN `source_file_id` bigint DEFAULT NULL COMMENT '来源文件ID，PDF分页时为PDF文件ID',
    ADD COLUMN `source_page_no` int DEFAULT NULL COMMENT '来源文件页码，图片为1',
    ADD COLUMN `retry_count` int NOT NULL DEFAULT 0 COMMENT '页面OCR重试次数',
    ADD COLUMN `clean_status` tinyint NOT NULL DEFAULT 0 COMMENT '净化图状态：0未生成、1生成中、2已完成、4失败',
    ADD COLUMN `clean_fail_reason` varchar(500) DEFAULT NULL COMMENT '净化图失败原因';

ALTER TABLE `sys_question_capture_region`
    ADD COLUMN `manually_corrected` tinyint NOT NULL DEFAULT 0 COMMENT '是否人工校正：0否、1是',
    ADD COLUMN `wrong_question_id` bigint DEFAULT NULL COMMENT '确认后创建的错题ID',
    ADD UNIQUE KEY `uk_capture_region_wrong_question` (`wrong_question_id`);
