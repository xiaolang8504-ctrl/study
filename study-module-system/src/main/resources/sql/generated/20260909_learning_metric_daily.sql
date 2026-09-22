-- P1：学生学习趋势日快照。由 LearningMetricDailyService 每日幂等重建前一天数据。
CREATE TABLE IF NOT EXISTS `sys_learning_metric_daily` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `metric_date` date NOT NULL COMMENT '快照日期',
    `user_id` bigint NOT NULL COMMENT '学生用户ID',
    `subject` varchar(64) NOT NULL COMMENT '科目字典键值，ALL 表示全部科目',
    `wrong_question_count` int NOT NULL DEFAULT 0 COMMENT '错题总数',
    `pending_correction_count` int NOT NULL DEFAULT 0 COMMENT '待订正数量',
    `corrected_count` int NOT NULL DEFAULT 0 COMMENT '已订正数量',
    `mastered_count` int NOT NULL DEFAULT 0 COMMENT '已掌握数量',
    `archived_count` int NOT NULL DEFAULT 0 COMMENT '已归档数量',
    `mastery_rate` int NOT NULL DEFAULT 0 COMMENT '掌握率，百分比',
    `review_count` int NOT NULL DEFAULT 0 COMMENT '当天完成复习数',
    `judged_answer_count` int NOT NULL DEFAULT 0 COMMENT '当天已判定作答数',
    `correct_answer_count` int NOT NULL DEFAULT 0 COMMENT '当天答对数',
    `retention_rate` int NOT NULL DEFAULT 0 COMMENT '当天保持率，百分比',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_learning_metric_daily_user_subject` (`metric_date`, `user_id`, `subject`),
    KEY `idx_learning_metric_daily_user_date` (`user_id`, `metric_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生学习日快照';
