-- P1-02：独立作答只能由“答案曝光前已保存”的服务端凭证确认。
ALTER TABLE `sys_review_record`
    ADD COLUMN IF NOT EXISTS `is_independent` tinyint NOT NULL DEFAULT 0
    COMMENT '是否答案曝光前主动作答：0否，1是' AFTER `is_correct`;

CREATE INDEX IF NOT EXISTS `idx_review_record_user_independent_time`
    ON `sys_review_record` (`user_id`, `is_independent`, `review_time`);
