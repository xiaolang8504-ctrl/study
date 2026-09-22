-- P2：家长 PC Web 的学生—监护人受控绑定及操作审计。
CREATE TABLE IF NOT EXISTS `student_guardian_rel` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `student_user_id` bigint NOT NULL COMMENT '学生用户ID',
    `guardian_user_id` bigint DEFAULT NULL COMMENT '家长用户ID，接受邀请前为空',
    `relation_type` varchar(32) NOT NULL DEFAULT 'GUARDIAN' COMMENT '关系类型',
    `status` tinyint NOT NULL COMMENT '0待家长接受，1待学生确认，2有效，3已解绑',
    `invitation_code_hash` char(64) DEFAULT NULL COMMENT '一次性邀请码SHA-256摘要',
    `invitation_expire_time` datetime DEFAULT NULL COMMENT '邀请码过期时间',
    `inviter_user_id` bigint NOT NULL COMMENT '邀请人用户ID',
    `accepter_user_id` bigint DEFAULT NULL COMMENT '接受邀请的家长用户ID',
    `confirmer_user_id` bigint DEFAULT NULL COMMENT '确认绑定的学生用户ID',
    `revoker_user_id` bigint DEFAULT NULL COMMENT '解绑操作人用户ID',
    `accept_time` datetime DEFAULT NULL COMMENT '家长接受时间',
    `confirm_time` datetime DEFAULT NULL COMMENT '学生确认时间',
    `revoke_time` datetime DEFAULT NULL COMMENT '解绑时间',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_guardian_invitation_code_hash` (`invitation_code_hash`),
    KEY `idx_guardian_student_status` (`guardian_user_id`, `status`, `student_user_id`),
    KEY `idx_student_guardian_status` (`student_user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生家长受控绑定关系';

CREATE TABLE IF NOT EXISTS `guardian_access_audit` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `relation_id` bigint DEFAULT NULL COMMENT '监护关系ID',
    `student_user_id` bigint DEFAULT NULL COMMENT '学生用户ID',
    `guardian_user_id` bigint DEFAULT NULL COMMENT '家长用户ID',
    `operator_user_id` bigint NOT NULL COMMENT '实际操作人用户ID',
    `action_type` varchar(64) NOT NULL COMMENT '操作类型',
    `operation_result` varchar(16) NOT NULL COMMENT '操作结果',
    `source` varchar(32) NOT NULL COMMENT '操作来源',
    `detail` varchar(500) DEFAULT NULL COMMENT '审计详情',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_guardian_audit_relation_time` (`relation_id`, `create_time`),
    KEY `idx_guardian_audit_operator_time` (`operator_user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭协作访问审计';

CREATE TABLE IF NOT EXISTS `guardian_assisted_capture` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `relation_id` bigint DEFAULT NULL COMMENT '预留关系ID',
    `student_user_id` bigint NOT NULL COMMENT '学生用户ID',
    `guardian_user_id` bigint NOT NULL COMMENT '家长用户ID',
    `grade` varchar(64) NOT NULL, `subject` varchar(64) NOT NULL,
    `question_type` varchar(64) NOT NULL, `source` varchar(64) NOT NULL,
    `learning_point` varchar(255) DEFAULT NULL, `error_labels` varchar(500) DEFAULT NULL,
    `source_file_ids` varchar(1000) NOT NULL COMMENT '家长原始文件ID列表',
    `status` tinyint NOT NULL COMMENT '0待学生确认，1已创建采集任务，2已撤销',
    `confirmed_by_user_id` bigint DEFAULT NULL, `capture_task_id` bigint DEFAULT NULL,
    `confirm_time` datetime DEFAULT NULL, `revoke_time` datetime DEFAULT NULL,
    `create_time` datetime NOT NULL, `update_time` datetime NOT NULL,
    PRIMARY KEY (`id`), KEY `idx_assisted_capture_student_status` (`student_user_id`, `status`),
    KEY `idx_assisted_capture_guardian_status` (`guardian_user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家长代上传待学生确认任务';

CREATE TABLE IF NOT EXISTS `guardian_weekly_report_subscription` (
 `id` bigint NOT NULL AUTO_INCREMENT, `guardian_user_id` bigint NOT NULL, `student_user_id` bigint NOT NULL,
 `site_notification_enabled` tinyint NOT NULL DEFAULT 1, `email_enabled` tinyint NOT NULL DEFAULT 0,
 `last_sent_week` date DEFAULT NULL, `create_time` datetime NOT NULL, `update_time` datetime NOT NULL,
 PRIMARY KEY (`id`), UNIQUE KEY `uk_guardian_weekly_report_subscription` (`guardian_user_id`,`student_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家长周报提醒订阅';
