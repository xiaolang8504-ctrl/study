-- F3-02：版本化知识前置关系；F3-03 复用既有 question_variant.sql 的模板/变量/公式/校验/审计表。
CREATE TABLE IF NOT EXISTS `sys_knowledge_point_prerequisite` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关系ID',
    `knowledge_point_id` bigint NOT NULL COMMENT '当前知识点ID',
    `prerequisite_point_id` bigint NOT NULL COMMENT '前置知识点ID',
    `relation_version` varchar(50) NOT NULL DEFAULT 'v1' COMMENT '关系版本',
    `enable` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_point_prerequisite` (`knowledge_point_id`, `prerequisite_point_id`),
    KEY `idx_prerequisite_reverse` (`prerequisite_point_id`, `enable`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识点前置关系';

-- 兼容旧库未执行 question_variant.sql 的情况；字段与该基础模型保持一致。
CREATE TABLE IF NOT EXISTS `sys_question_variant_template` (
 `id` bigint NOT NULL AUTO_INCREMENT, `template_code` varchar(64) NOT NULL, `template_name` varchar(120) NOT NULL,
 `original_question_id` bigint NOT NULL, `subject` varchar(30) NOT NULL, `grade` varchar(20) NOT NULL,
 `knowledge_point_id` bigint DEFAULT NULL, `question_type` varchar(30) NOT NULL, `title_template` text NOT NULL,
 `answer_template` text DEFAULT NULL, `analysis_template` longtext, `current_version` int NOT NULL DEFAULT 1,
 `status` tinyint NOT NULL DEFAULT 0, `reviewer_id` bigint DEFAULT NULL, `review_time` datetime DEFAULT NULL,
 `review_remark` varchar(500) DEFAULT NULL, `create_id` bigint NOT NULL, `update_id` bigint DEFAULT NULL,
 `deleted` tinyint NOT NULL DEFAULT 0, `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, PRIMARY KEY (`id`),
 UNIQUE KEY `uk_question_variant_template_code` (`template_code`), KEY `idx_question_variant_template_filter` (`subject`,`grade`,`question_type`,`status`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数变式模板';
CREATE TABLE IF NOT EXISTS `sys_question_variant_template_version` (
 `id` bigint NOT NULL AUTO_INCREMENT, `template_id` bigint NOT NULL, `version_no` int NOT NULL, `snapshot_json` longtext NOT NULL,
 `change_summary` varchar(500) DEFAULT NULL, `version_status` tinyint NOT NULL DEFAULT 0, `operator_id` bigint NOT NULL,
 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`), UNIQUE KEY `uk_question_variant_template_version` (`template_id`,`version_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数变式模板版本';
CREATE TABLE IF NOT EXISTS `sys_question_variant_variable` (
 `id` bigint NOT NULL AUTO_INCREMENT, `template_id` bigint NOT NULL, `version_no` int NOT NULL, `variable_code` varchar(64) NOT NULL,
 `variable_name` varchar(100) NOT NULL, `value_type` varchar(20) NOT NULL, `min_value` decimal(30,10) DEFAULT NULL,
 `max_value` decimal(30,10) DEFAULT NULL, `step_value` decimal(30,10) DEFAULT NULL, `decimal_scale` int DEFAULT NULL,
 `enum_values_json` text DEFAULT NULL, `unit_type` varchar(30) DEFAULT NULL, `unit` varchar(30) DEFAULT NULL,
 `is_required` tinyint NOT NULL DEFAULT 1, `sort` int NOT NULL DEFAULT 0, `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, PRIMARY KEY (`id`),
 UNIQUE KEY `uk_question_variant_variable` (`template_id`,`version_no`,`variable_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数变式变量配置';
CREATE TABLE IF NOT EXISTS `sys_question_variant_formula` (
 `id` bigint NOT NULL AUTO_INCREMENT, `template_id` bigint NOT NULL, `version_no` int NOT NULL, `formula_code` varchar(64) NOT NULL,
 `formula_name` varchar(100) NOT NULL, `expression` text NOT NULL, `target_variable` varchar(64) NOT NULL,
 `execution_order` int NOT NULL DEFAULT 0, `precision_scale` int DEFAULT NULL, `rounding_mode` varchar(30) DEFAULT NULL,
 `enable` tinyint NOT NULL DEFAULT 1, `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, PRIMARY KEY (`id`),
 UNIQUE KEY `uk_question_variant_formula` (`template_id`,`version_no`,`formula_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数变式计算公式';
CREATE TABLE IF NOT EXISTS `sys_question_variant_record` (
 `id` bigint NOT NULL AUTO_INCREMENT, `batch_id` bigint NOT NULL DEFAULT 0, `template_id` bigint NOT NULL, `template_version` int NOT NULL,
 `original_question_id` bigint NOT NULL, `bank_question_id` bigint DEFAULT NULL, `source_record_id` bigint DEFAULT NULL,
 `question_content` longtext NOT NULL, `correct_answer` longtext NOT NULL, `analysis` longtext, `question_hash` varchar(32) DEFAULT NULL,
 `validation_status` varchar(20) NOT NULL DEFAULT 'PENDING', `audit_status` tinyint NOT NULL DEFAULT 0, `audit_remark` varchar(500) DEFAULT NULL,
 `create_id` bigint NOT NULL, `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP, `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY (`id`), UNIQUE KEY `uk_question_variant_bank_question` (`bank_question_id`), KEY `idx_question_variant_record_template` (`template_id`,`template_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数变式题记录';
CREATE TABLE IF NOT EXISTS `sys_question_variant_parameter` (
 `id` bigint NOT NULL AUTO_INCREMENT, `variant_record_id` bigint NOT NULL, `variable_code` varchar(64) NOT NULL, `variable_name` varchar(100) NOT NULL,
 `value_text` varchar(255) NOT NULL, `numeric_value` decimal(30,10) DEFAULT NULL, `display_value` varchar(255) NOT NULL, `unit` varchar(30) DEFAULT NULL,
 `generate_source` varchar(30) NOT NULL DEFAULT 'RANDOM', `sort` int NOT NULL DEFAULT 0, `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (`id`), UNIQUE KEY `uk_question_variant_parameter` (`variant_record_id`,`variable_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数变式题参数快照';
CREATE TABLE IF NOT EXISTS `sys_question_variant_validation` (
 `id` bigint NOT NULL AUTO_INCREMENT, `variant_record_id` bigint NOT NULL, `validation_code` varchar(64) NOT NULL, `validation_type` varchar(30) NOT NULL,
 `validation_level` varchar(20) NOT NULL, `validation_status` varchar(20) NOT NULL, `input_snapshot` text DEFAULT NULL, `message` varchar(1000) DEFAULT NULL,
 `execution_order` int NOT NULL DEFAULT 0, `duration_ms` int NOT NULL DEFAULT 0, `validate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY (`id`), KEY `idx_question_variant_validation_record` (`variant_record_id`,`execution_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数变式题校验明细';
CREATE TABLE IF NOT EXISTS `sys_question_variant_audit` (
 `id` bigint NOT NULL AUTO_INCREMENT, `variant_record_id` bigint NOT NULL, `audit_action` varchar(30) NOT NULL,
 `audit_status` tinyint NOT NULL, `before_snapshot` longtext, `after_snapshot` longtext, `audit_remark` varchar(500) DEFAULT NULL,
 `auditor_id` bigint NOT NULL, `audit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (`id`),
 KEY `idx_question_variant_audit_record` (`variant_record_id`,`audit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='参数变式题审核记录';

-- 单题即时生成不创建异步批次，使用 0 作为系统即时批次标识。
ALTER TABLE `sys_question_variant_record` MODIFY COLUMN `batch_id` bigint NOT NULL DEFAULT 0 COMMENT '生成批次ID';

ALTER TABLE `sys_question_bank`
    ADD COLUMN `variant_template_id` bigint DEFAULT NULL COMMENT '参数化模板ID' AFTER `license`,
    ADD COLUMN `variant_record_id` bigint DEFAULT NULL COMMENT '参数化生成记录ID' AFTER `variant_template_id`,
    ADD KEY `idx_question_bank_variant_record` (`variant_record_id`);

SET SESSION group_concat_max_len = 8192;
SET @qb_root = (SELECT `id` FROM `sys_resource` WHERE `code`='system:questionBank' LIMIT 1);
SET @review_root = (SELECT `id` FROM `sys_resource` WHERE `code`='system:review' LIMIT 1);
INSERT IGNORE INTO `sys_resource` (`resource_name`,`code`,`pid`,`sort`,`create_time`) VALUES
('保存知识点前置关系','system:questionBank:saveKnowledgePointPrerequisite',@qb_root,160,NOW()),
('知识点前置关系列表','system:questionBank:knowledgePointPrerequisiteList',@qb_root,161,NOW()),
('变式题模板列表','system:questionBank:questionVariantTemplateList',@qb_root,162,NOW()),
('保存变式题模板','system:questionBank:saveQuestionVariantTemplate',@qb_root,163,NOW()),
('生成变式题','system:questionBank:generateQuestionVariant',@qb_root,164,NOW()),
('个人知识学习路径','system:review:learningPath',@review_root,165,NOW());
SET @qb_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='questionBank' LIMIT 1);
SET @review_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='intelligentReview' LIMIT 1);
INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @qb_menu, `id`, CONCAT(@qb_root,'-',`id`), NOW() FROM `sys_resource`
WHERE `code` IN ('system:questionBank:saveKnowledgePointPrerequisite','system:questionBank:knowledgePointPrerequisiteList',
                 'system:questionBank:questionVariantTemplateList','system:questionBank:saveQuestionVariantTemplate','system:questionBank:generateQuestionVariant');
INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @review_menu, `id`, CONCAT(@review_root,'-',`id`), NOW() FROM `sys_resource` WHERE `code`='system:review:learningPath';
UPDATE `sys_menu` m SET m.`resource_ids`=(SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`),
 m.`resource_level`=(SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`)
WHERE m.`id` IN (@qb_menu,@review_menu);
-- 权限缓存仅在登录时构建；部署迁移后需重新登录。
