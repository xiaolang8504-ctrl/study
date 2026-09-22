-- 复习真实作答与正确率升级
-- 适用数据库：MySQL 8.0+

ALTER TABLE `sys_review_record`
    ADD COLUMN `student_answer` text COMMENT '学生本次答案快照'
        AFTER `answer_duration_seconds`,
    ADD COLUMN `is_correct` tinyint unsigned DEFAULT NULL COMMENT '本次作答是否正确: 0错误, 1正确'
        AFTER `student_answer`,
    ADD COLUMN `answer_judge_type` tinyint unsigned DEFAULT NULL COMMENT '判定来源: 0学生自评, 1系统自动判定'
        AFTER `is_correct`,
    ADD KEY `idx_review_record_user_correct` (`user_id`, `is_correct`);
