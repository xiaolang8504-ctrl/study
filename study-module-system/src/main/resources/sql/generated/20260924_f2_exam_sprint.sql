-- F2-07：考前冲刺设置。该表只保存考试约束和短练预算，不修改 review_item 的正常间隔排期。
CREATE TABLE IF NOT EXISTS `sys_review_exam_sprint` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `user_id` bigint NOT NULL,
    `subject` varchar(50) NOT NULL,
    `exam_date` date NOT NULL,
    `scope_text` varchar(500) DEFAULT NULL,
    `daily_minutes` int NOT NULL,
    `target_question_count` int NOT NULL,
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '0关闭，1启用',
    `create_time` datetime NOT NULL,
    `update_time` datetime NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_review_exam_sprint_user_subject` (`user_id`, `subject`),
    KEY `idx_review_exam_sprint_user_date` (`user_id`, `exam_date`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生考前冲刺约束';
