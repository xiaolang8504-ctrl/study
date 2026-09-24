-- F2-02：结构化错因与能力层级。保留 error_labels 作为历史兼容与学生自定义补充。
ALTER TABLE `sys_wrong_question`
    ADD COLUMN `error_cause_codes` varchar(100) DEFAULT NULL COMMENT '结构化错因编码，逗号分隔：READING、CONCEPT、METHOD、CALCULATION、EXPRESSION' AFTER `error_labels`,
    ADD COLUMN `ability_level` varchar(20) DEFAULT NULL COMMENT '能力层级：FOUNDATION基础、APPLICATION应用、COMPREHENSIVE综合' AFTER `error_cause_codes`,
    ADD KEY `idx_wrong_question_diagnosis_filter` (`create_id`, `subject`, `ability_level`, `status`);
