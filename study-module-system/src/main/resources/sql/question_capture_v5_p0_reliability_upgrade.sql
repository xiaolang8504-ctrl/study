ALTER TABLE `sys_question_capture_task`
    ADD COLUMN `client_request_id` varchar(64) DEFAULT NULL COMMENT '客户端幂等请求标识';

ALTER TABLE `sys_question_capture_task`
    ADD UNIQUE KEY `uk_capture_task_user_request` (`create_id`, `client_request_id`);

ALTER TABLE `sys_question_capture_region`
    ADD COLUMN `grade` varchar(32) DEFAULT NULL COMMENT '题块年级覆盖值',
    ADD COLUMN `subject` varchar(32) DEFAULT NULL COMMENT '题块科目覆盖值',
    ADD COLUMN `question_type` varchar(32) DEFAULT NULL COMMENT '题块题型覆盖值',
    ADD COLUMN `source` varchar(32) DEFAULT NULL COMMENT '题块来源覆盖值',
    ADD COLUMN `learning_point` varchar(500) DEFAULT NULL COMMENT '题块知识点覆盖值',
    ADD COLUMN `error_labels` varchar(500) DEFAULT NULL COMMENT '题块错因标签覆盖值';
