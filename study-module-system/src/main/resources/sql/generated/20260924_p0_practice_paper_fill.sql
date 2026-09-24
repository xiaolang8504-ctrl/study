-- P0-7：纸面练习卷逐题回填。在线作答记录与纸面作答记录分表保存，避免互相覆盖。
CREATE TABLE IF NOT EXISTS `sys_practice_paper_answer_record` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` bigint NOT NULL COMMENT '练习会话ID',
    `paper_version` int NOT NULL DEFAULT 1 COMMENT '练习卷版本',
    `session_question_id` bigint NOT NULL COMMENT '练习题目ID',
    `user_id` bigint NOT NULL COMMENT '学生ID',
    `attempt_no` int NOT NULL DEFAULT 1 COMMENT '本练习卷第几次纸面作答',
    `wrong_question_id` bigint DEFAULT NULL COMMENT '关联错题ID',
    `answer_status` varchar(16) NOT NULL COMMENT 'CORRECT、WRONG、UNANSWERED',
    `student_answer` text DEFAULT NULL COMMENT '纸面答案或解题步骤',
    `error_reason` varchar(500) DEFAULT NULL COMMENT '错因说明',
    `duration_seconds` int NOT NULL DEFAULT 0 COMMENT '作答用时，秒',
    `answer_file_id` bigint DEFAULT NULL COMMENT '做完的纸面练习卷附件文件ID',
    `answer_time` datetime NOT NULL COMMENT '学生实际纸面作答时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_practice_paper_answer_attempt` (`session_question_id`, `attempt_no`),
    KEY `idx_practice_paper_answer_session_user` (`session_id`, `user_id`, `attempt_no`),
    KEY `idx_practice_paper_answer_wrong_question` (`wrong_question_id`, `answer_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='纸面练习卷逐题回填记录';
