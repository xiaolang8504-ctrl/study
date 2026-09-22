-- 参数变式题基础数据模型（MySQL 8.0+，可重复执行）

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `sys_question_variant_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '变式模板ID',
  `template_code` varchar(64) NOT NULL COMMENT '模板唯一编码',
  `template_name` varchar(120) NOT NULL COMMENT '模板名称',
  `original_question_id` bigint NOT NULL COMMENT '关联原题ID',
  `subject` varchar(30) NOT NULL COMMENT '学科字典键值',
  `grade` varchar(20) NOT NULL COMMENT '年级字典键值',
  `knowledge_point_id` bigint DEFAULT NULL COMMENT '主知识点ID',
  `question_type` varchar(30) NOT NULL COMMENT '题型字典键值',
  `title_template` text NOT NULL COMMENT '参数化题干模板',
  `answer_template` text DEFAULT NULL COMMENT '答案展示模板',
  `analysis_template` longtext COMMENT '解析模板',
  `current_version` int NOT NULL DEFAULT 1 COMMENT '当前版本号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0草稿，1启用，2停用',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `review_remark` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `create_id` bigint NOT NULL COMMENT '创建人ID',
  `update_id` bigint DEFAULT NULL COMMENT '最后修改人ID',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否，1是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_variant_template_code` (`template_code`),
  KEY `idx_question_variant_template_filter` (`subject`,`grade`,`question_type`,`status`,`deleted`),
  KEY `idx_question_variant_template_original` (`original_question_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数变式模板';

CREATE TABLE IF NOT EXISTS `sys_question_variant_template_version` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板版本ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `version_no` int NOT NULL COMMENT '版本号',
  `snapshot_json` longtext NOT NULL COMMENT '模板、变量、公式和约束完整JSON快照',
  `change_summary` varchar(500) DEFAULT NULL COMMENT '版本变更摘要',
  `version_status` tinyint NOT NULL DEFAULT 0 COMMENT '版本状态：0草稿，1已发布，2已废弃',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_variant_template_version` (`template_id`,`version_no`),
  KEY `idx_question_variant_version_status` (`template_id`,`version_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数变式模板版本';

CREATE TABLE IF NOT EXISTS `sys_question_variant_variable` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '变量ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `version_no` int NOT NULL COMMENT '所属模板版本',
  `variable_code` varchar(64) NOT NULL COMMENT '变量编码',
  `variable_name` varchar(100) NOT NULL COMMENT '变量名称',
  `value_type` varchar(20) NOT NULL COMMENT '取值类型：INTEGER、DECIMAL、FRACTION、ENUM',
  `min_value` decimal(30,10) DEFAULT NULL COMMENT '最小值',
  `max_value` decimal(30,10) DEFAULT NULL COMMENT '最大值',
  `step_value` decimal(30,10) DEFAULT NULL COMMENT '步长',
  `decimal_scale` int DEFAULT NULL COMMENT '小数位数',
  `enum_values_json` text DEFAULT NULL COMMENT '枚举候选值JSON',
  `unit_type` varchar(30) DEFAULT NULL COMMENT '单位维度类型',
  `unit` varchar(30) DEFAULT NULL COMMENT '默认单位',
  `is_required` tinyint NOT NULL DEFAULT 1 COMMENT '是否必填：0否，1是',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_variant_variable` (`template_id`,`version_no`,`variable_code`),
  KEY `idx_question_variant_variable_order` (`template_id`,`version_no`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数变式变量配置';

CREATE TABLE IF NOT EXISTS `sys_question_variant_formula` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公式ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `version_no` int NOT NULL COMMENT '所属模板版本',
  `formula_code` varchar(64) NOT NULL COMMENT '公式编码',
  `formula_name` varchar(100) NOT NULL COMMENT '公式名称',
  `expression` text NOT NULL COMMENT '受控公式表达式',
  `target_variable` varchar(64) NOT NULL COMMENT '结果变量编码',
  `execution_order` int NOT NULL DEFAULT 0 COMMENT '执行顺序',
  `precision_scale` int DEFAULT NULL COMMENT '结果小数位数',
  `rounding_mode` varchar(30) DEFAULT NULL COMMENT '取整模式',
  `enable` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态：0停用，1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_variant_formula` (`template_id`,`version_no`,`formula_code`),
  KEY `idx_question_variant_formula_order` (`template_id`,`version_no`,`enable`,`execution_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数变式计算公式';

CREATE TABLE IF NOT EXISTS `sys_question_variant_constraint` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '约束ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `version_no` int NOT NULL COMMENT '所属模板版本',
  `constraint_code` varchar(64) NOT NULL COMMENT '约束编码',
  `constraint_type` varchar(30) NOT NULL COMMENT '约束类型',
  `expression` text NOT NULL COMMENT '受控约束表达式',
  `constraint_level` varchar(20) NOT NULL DEFAULT 'STRONG' COMMENT '级别：STRONG强约束，WEAK弱约束',
  `error_message` varchar(500) NOT NULL COMMENT '失败提示',
  `execution_order` int NOT NULL DEFAULT 0 COMMENT '执行顺序',
  `enable` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态：0停用，1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_variant_constraint` (`template_id`,`version_no`,`constraint_code`),
  KEY `idx_question_variant_constraint_order` (`template_id`,`version_no`,`enable`,`execution_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数变式约束规则';

CREATE TABLE IF NOT EXISTS `sys_question_variant_batch` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '生成批次ID',
  `batch_no` varchar(64) NOT NULL COMMENT '生成批次号',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `template_version` int NOT NULL COMMENT '模板版本号',
  `generate_mode` varchar(30) NOT NULL DEFAULT 'NUMBER' COMMENT '生成模式：NUMBER数字变式，CONDITION条件变式',
  `request_count` int NOT NULL COMMENT '请求生成数量',
  `processed_count` int NOT NULL DEFAULT 0 COMMENT '已处理数量',
  `success_count` int NOT NULL DEFAULT 0 COMMENT '成功数量',
  `failure_count` int NOT NULL DEFAULT 0 COMMENT '失败数量',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING、RUNNING、SUCCESS、PARTIAL、FAILED、CANCELLED',
  `config_json` text DEFAULT NULL COMMENT '本次生成配置JSON',
  `error_message` varchar(1000) DEFAULT NULL COMMENT '任务异常信息',
  `create_id` bigint NOT NULL COMMENT '创建人ID',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `finish_time` datetime DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_variant_batch_no` (`batch_no`),
  KEY `idx_question_variant_batch_query` (`create_id`,`status`,`create_time`),
  KEY `idx_question_variant_batch_template` (`template_id`,`template_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数变式生成批次';

CREATE TABLE IF NOT EXISTS `sys_question_variant_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '变式题记录ID',
  `batch_id` bigint NOT NULL COMMENT '生成批次ID',
  `template_id` bigint NOT NULL COMMENT '模板ID',
  `template_version` int NOT NULL COMMENT '模板版本号',
  `original_question_id` bigint NOT NULL COMMENT '原题ID',
  `bank_question_id` bigint DEFAULT NULL COMMENT '入库后的正式题目ID',
  `source_record_id` bigint DEFAULT NULL COMMENT '重新生成来源记录ID',
  `question_content` longtext NOT NULL COMMENT '生成题干',
  `correct_answer` longtext NOT NULL COMMENT '生成答案',
  `analysis` longtext COMMENT '生成解析',
  `question_hash` varchar(32) DEFAULT NULL COMMENT '规范化题干MD5',
  `validation_status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '校验状态：PENDING、PASS、SUSPICIOUS、FAIL、ERROR',
  `audit_status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核，1通过，2退回',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `create_id` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_variant_bank_question` (`bank_question_id`),
  KEY `idx_question_variant_record_batch` (`batch_id`,`validation_status`,`audit_status`),
  KEY `idx_question_variant_record_original` (`original_question_id`,`question_hash`),
  KEY `idx_question_variant_record_template` (`template_id`,`template_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数变式题记录';

CREATE TABLE IF NOT EXISTS `sys_question_variant_parameter` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目参数快照ID',
  `variant_record_id` bigint NOT NULL COMMENT '变式题记录ID',
  `variable_code` varchar(64) NOT NULL COMMENT '变量编码',
  `variable_name` varchar(100) NOT NULL COMMENT '变量名称',
  `value_text` varchar(255) NOT NULL COMMENT '原始参数值',
  `numeric_value` decimal(30,10) DEFAULT NULL COMMENT '可计算数值',
  `display_value` varchar(255) NOT NULL COMMENT '题干展示值',
  `unit` varchar(30) DEFAULT NULL COMMENT '参数单位',
  `generate_source` varchar(30) NOT NULL DEFAULT 'RANDOM' COMMENT '来源：RANDOM随机，MANUAL人工，DERIVED推导',
  `sort` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_variant_parameter` (`variant_record_id`,`variable_code`),
  KEY `idx_question_variant_parameter_order` (`variant_record_id`,`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数变式题参数快照';

CREATE TABLE IF NOT EXISTS `sys_question_variant_calculation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '计算过程ID',
  `variant_record_id` bigint NOT NULL COMMENT '变式题记录ID',
  `formula_id` bigint DEFAULT NULL COMMENT '公式ID',
  `formula_code` varchar(64) NOT NULL COMMENT '公式编码',
  `step_no` int NOT NULL COMMENT '计算步骤',
  `expression` text NOT NULL COMMENT '实际执行表达式',
  `input_json` text DEFAULT NULL COMMENT '输入参数JSON',
  `raw_result` varchar(500) DEFAULT NULL COMMENT '原始结果',
  `numeric_result` decimal(30,10) DEFAULT NULL COMMENT '数值结果',
  `formatted_result` varchar(500) DEFAULT NULL COMMENT '格式化结果',
  `result_unit` varchar(30) DEFAULT NULL COMMENT '结果单位',
  `execute_status` varchar(20) NOT NULL COMMENT '执行状态：SUCCESS、FAILED、SKIPPED',
  `error_message` varchar(1000) DEFAULT NULL COMMENT '计算异常信息',
  `duration_ms` int NOT NULL DEFAULT 0 COMMENT '执行耗时毫秒',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_variant_calculation_step` (`variant_record_id`,`step_no`),
  KEY `idx_question_variant_calculation_formula` (`formula_id`,`execute_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数变式题计算过程';

CREATE TABLE IF NOT EXISTS `sys_question_variant_validation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '校验记录ID',
  `variant_record_id` bigint NOT NULL COMMENT '变式题记录ID',
  `validation_code` varchar(64) NOT NULL COMMENT '校验项编码',
  `validation_type` varchar(30) NOT NULL COMMENT '校验类型',
  `validation_level` varchar(20) NOT NULL COMMENT '级别：ERROR、WARNING、INFO',
  `validation_status` varchar(20) NOT NULL COMMENT '状态：PASS、FAIL、ERROR',
  `input_snapshot` text DEFAULT NULL COMMENT '校验输入快照JSON',
  `message` varchar(1000) DEFAULT NULL COMMENT '校验结果说明',
  `execution_order` int NOT NULL DEFAULT 0 COMMENT '执行顺序',
  `duration_ms` int NOT NULL DEFAULT 0 COMMENT '执行耗时毫秒',
  `validate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '校验时间',
  PRIMARY KEY (`id`),
  KEY `idx_question_variant_validation_record` (`variant_record_id`,`execution_order`),
  KEY `idx_question_variant_validation_result` (`validation_status`,`validation_level`,`validate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数变式题校验明细';

CREATE TABLE IF NOT EXISTS `sys_question_variant_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审核记录ID',
  `variant_record_id` bigint NOT NULL COMMENT '变式题记录ID',
  `audit_action` varchar(30) NOT NULL COMMENT '操作：PASS、REJECT、MODIFY、RECHECK',
  `audit_status` tinyint NOT NULL COMMENT '审核结果：0待审核，1通过，2退回',
  `before_snapshot` longtext COMMENT '操作前快照JSON',
  `after_snapshot` longtext COMMENT '操作后快照JSON',
  `audit_remark` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `auditor_id` bigint NOT NULL COMMENT '审核人ID',
  `audit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  PRIMARY KEY (`id`),
  KEY `idx_question_variant_audit_record` (`variant_record_id`,`audit_time`),
  KEY `idx_question_variant_audit_user` (`auditor_id`,`audit_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='参数变式题审核记录';
