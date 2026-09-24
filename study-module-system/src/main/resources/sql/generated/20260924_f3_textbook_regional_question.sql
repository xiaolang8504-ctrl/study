-- F3-01：教材同步与地区真题。历史题为空表示未归类，不影响旧题使用。
ALTER TABLE `sys_question_bank`
    ADD COLUMN `textbook_version` varchar(100) DEFAULT NULL COMMENT '教材版本' AFTER `source_name`,
    ADD COLUMN `chapter_name` varchar(200) DEFAULT NULL COMMENT '教材章节' AFTER `textbook_version`,
    ADD COLUMN `region` varchar(100) DEFAULT NULL COMMENT '地区' AFTER `chapter_name`,
    ADD COLUMN `exam_year` int DEFAULT NULL COMMENT '真题年份' AFTER `region`,
    ADD COLUMN `paper_type` varchar(50) DEFAULT NULL COMMENT '卷型' AFTER `exam_year`,
    ADD COLUMN `license_version` varchar(50) DEFAULT NULL COMMENT '授权版本' AFTER `license`,
    ADD KEY `idx_question_bank_textbook_region` (`grade`, `subject`, `textbook_version`, `region`, `exam_year`, `review_status`, `enable`);
