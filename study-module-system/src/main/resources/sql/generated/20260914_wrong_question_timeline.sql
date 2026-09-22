CREATE TABLE IF NOT EXISTS `sys_wrong_question_timeline` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `wrong_question_id` bigint NOT NULL COMMENT '错题ID',
    `event_type` varchar(64) NOT NULL COMMENT '事件类型',
    `event_source` varchar(32) NOT NULL COMMENT '动作来源：STUDENT、OCR、AI、SYSTEM',
    `event_content` varchar(500) DEFAULT NULL COMMENT '事件说明',
    `create_id` bigint DEFAULT NULL COMMENT '实际操作者ID',
    `create_time` datetime NOT NULL COMMENT '发生时间',
    PRIMARY KEY (`id`),
    KEY `idx_wrong_question_timeline` (`wrong_question_id`,`create_time`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='错题学习证据时间线';
