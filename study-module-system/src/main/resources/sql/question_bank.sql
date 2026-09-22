-- 精品题库、知识点、审核、相似题练习一期（MySQL 8.0+，可重复执行）

CREATE TABLE IF NOT EXISTS `sys_knowledge_point` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识点ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父知识点ID，0表示根节点',
  `point_code` varchar(80) NOT NULL COMMENT '知识点唯一编码',
  `point_name` varchar(100) NOT NULL COMMENT '知识点名称',
  `grade` varchar(20) NOT NULL COMMENT '年级字典键值',
  `subject` varchar(30) NOT NULL COMMENT '科目字典键值',
  `sort` int NOT NULL DEFAULT 0 COMMENT '同级排序号',
  `enable` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态：0停用，1启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_knowledge_point_code` (`point_code`),
  KEY `idx_knowledge_point_subject_grade` (`subject`,`grade`,`enable`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='知识点目录';

CREATE TABLE IF NOT EXISTS `sys_question_bank` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题库题目ID',
  `grade` varchar(20) NOT NULL COMMENT '年级字典键值',
  `grade_name` varchar(30) DEFAULT NULL COMMENT '年级名称',
  `subject` varchar(30) NOT NULL COMMENT '科目字典键值',
  `subject_name` varchar(30) DEFAULT NULL COMMENT '科目名称',
  `question_type` varchar(30) NOT NULL COMMENT '题型字典键值',
  `question_type_name` varchar(30) DEFAULT NULL COMMENT '题型名称',
  `question_title` varchar(255) NOT NULL COMMENT '题目标题',
  `question_content` text NOT NULL COMMENT '题干内容',
  `question_hash` varchar(32) DEFAULT NULL COMMENT '规范化题干MD5，用于重复题检测',
  `content_format` varchar(20) NOT NULL DEFAULT 'TEXT' COMMENT '内容格式：TEXT普通文本，LATEX公式',
  `image_urls` text DEFAULT NULL COMMENT '题目图片URL，多个使用逗号分隔',
  `options_json` text DEFAULT NULL COMMENT '选择题选项JSON',
  `correct_answer` text NOT NULL COMMENT '标准答案',
  `analysis` text DEFAULT NULL COMMENT '题目解析',
  `difficulty` tinyint NOT NULL DEFAULT 3 COMMENT '难度等级：1简单至5困难',
  `source_name` varchar(100) DEFAULT NULL COMMENT '题目来源名称',
  `review_status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核，1通过，2驳回',
  `review_remark` varchar(500) DEFAULT NULL COMMENT '审核备注或驳回原因',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核人用户ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `enable` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态：0停用，1启用',
  `create_id` bigint NOT NULL COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_question_bank_filter` (`subject`,`grade`,`question_type`,`review_status`,`enable`,`difficulty`),
  KEY `idx_question_bank_hash` (`question_hash`),
  FULLTEXT KEY `ft_question_bank_content` (`question_title`,`question_content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审核精品题库';

CREATE TABLE IF NOT EXISTS `sys_question_knowledge_point` (
  `question_id` bigint NOT NULL COMMENT '题库题目ID',
  `knowledge_point_id` bigint NOT NULL COMMENT '知识点ID',
  `is_primary` tinyint NOT NULL DEFAULT 0 COMMENT '是否主知识点：0否，1是',
  PRIMARY KEY (`question_id`,`knowledge_point_id`),
  KEY `idx_question_point_reverse` (`knowledge_point_id`,`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题目知识点关联';

CREATE TABLE IF NOT EXISTS `sys_question_recommendation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '推荐记录ID',
  `batch_no` varchar(40) NOT NULL COMMENT '单次推荐批次号',
  `user_id` bigint NOT NULL COMMENT '学生用户ID',
  `wrong_question_id` bigint NOT NULL COMMENT '推荐来源错题ID',
  `bank_question_id` bigint NOT NULL COMMENT '被推荐的题库题目ID',
  `rank_no` int NOT NULL COMMENT '题目在推荐批次中的排序',
  `recommend_score` decimal(8,2) NOT NULL DEFAULT 0 COMMENT '推荐匹配分数',
  `algorithm_version` varchar(30) NOT NULL DEFAULT 'SIMILAR_V2' COMMENT '推荐算法版本',
  `match_type` varchar(30) NOT NULL DEFAULT 'STRUCTURED' COMMENT '候选召回方式',
  `recommend_reason` varchar(255) DEFAULT NULL COMMENT '面向学生的推荐原因',
  `experiment_group` varchar(10) NOT NULL DEFAULT 'A' COMMENT '推荐A/B实验分组',
  `exposure_time` datetime NOT NULL COMMENT '题目曝光时间',
  `student_answer` text DEFAULT NULL COMMENT '学生提交答案',
  `is_correct` tinyint DEFAULT NULL COMMENT '是否回答正确：0错误，1正确，未作答为空',
  `judge_type` varchar(20) DEFAULT NULL COMMENT '判题方式：AUTO自动，SELF学生自评',
  `duration_seconds` int DEFAULT NULL COMMENT '本次作答耗时秒数',
  `answer_time` datetime DEFAULT NULL COMMENT '答案提交时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_recommend_user_time` (`user_id`,`create_time`),
  KEY `idx_recommend_question` (`bank_question_id`,`is_correct`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='相似题曝光作答记录';

CREATE TABLE IF NOT EXISTS `sys_question_report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报记录ID',
  `user_id` bigint NOT NULL COMMENT '举报学生用户ID',
  `bank_question_id` bigint NOT NULL COMMENT '被举报的题库题目ID',
  `report_type` varchar(30) NOT NULL COMMENT '举报类型编码',
  `report_content` varchar(500) DEFAULT NULL COMMENT '举报问题描述',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态：0待处理，1已处理，2无效',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人用户ID',
  `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  PRIMARY KEY (`id`),
  KEY `idx_question_report_status` (`status`,`create_time`),
  KEY `idx_question_report_question` (`bank_question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题目举报';

CREATE TABLE IF NOT EXISTS `sys_wrong_question_knowledge_point` (
  `wrong_question_id` bigint NOT NULL COMMENT '个人错题ID',
  `knowledge_point_id` bigint NOT NULL COMMENT '标准知识点ID',
  `relation_source` varchar(30) NOT NULL DEFAULT 'MANUAL' COMMENT '关联来源：MANUAL人工，AUTO_MIGRATION自动迁移',
  PRIMARY KEY (`wrong_question_id`,`knowledge_point_id`),
  KEY `idx_wrong_point_reverse` (`knowledge_point_id`,`wrong_question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='个人错题知识点标准关联';

CREATE TABLE IF NOT EXISTS `sys_question_bank_image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目图片关联ID',
  `question_id` bigint NOT NULL COMMENT '题库题目ID',
  `file_id` int DEFAULT NULL COMMENT 'sys_file文件ID',
  `image_url` varchar(1000) DEFAULT NULL COMMENT '兼容外部图片URL',
  `original_name` varchar(255) DEFAULT NULL COMMENT '图片原始文件名',
  `sort` int NOT NULL DEFAULT 1 COMMENT '图片展示顺序',
  `is_cover` tinyint NOT NULL DEFAULT 0 COMMENT '是否封面：0否，1是',
  PRIMARY KEY (`id`),
  KEY `idx_question_bank_image_question` (`question_id`,`sort`),
  KEY `idx_question_bank_image_file` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题库题目图片文件关联';

-- 菜单：题库管理面向管理角色，相似题练习面向已有错题本角色。
INSERT IGNORE INTO `sys_menu`
(`menu_name`,`code`,`pid`,`level`,`sort`,`resource_ids`,`resource_level`,`create_time`)
VALUES ('题库管理','questionBank',0,1,5,'','',NOW()),
       ('相似题练习','similarPractice',0,1,6,'','',NOW());

INSERT IGNORE INTO `sys_resource`
(`resource_name`,`code`,`pid`,`sort`,`create_time`)
VALUES ('精品题库','system:questionBank',0,100,NOW());
SET @qb_root = (SELECT `id` FROM `sys_resource` WHERE `code`='system:questionBank' LIMIT 1);

INSERT IGNORE INTO `sys_resource` (`resource_name`,`code`,`pid`,`sort`,`create_time`) VALUES
('题库分页列表','system:questionBank:questionBankPageList',@qb_root,101,NOW()),
('题库详情','system:questionBank:questionBankDetail',@qb_root,102,NOW()),
('保存题库题目','system:questionBank:saveQuestionBank',@qb_root,103,NOW()),
('删除题库题目','system:questionBank:deleteQuestionBank',@qb_root,104,NOW()),
('审核题库题目','system:questionBank:reviewQuestionBank',@qb_root,105,NOW()),
('知识点列表','system:questionBank:knowledgePointList',@qb_root,106,NOW()),
('保存知识点','system:questionBank:saveKnowledgePoint',@qb_root,107,NOW()),
('获取审核相似题','system:questionBank:similarQuestionList',@qb_root,108,NOW()),
('提交相似题作答','system:questionBank:submitQuestionPractice',@qb_root,109,NOW()),
('举报题目','system:questionBank:reportQuestion',@qb_root,110,NOW()),
('题目举报分页列表','system:questionBank:questionReportPageList',@qb_root,111,NOW()),
('处理题目举报','system:questionBank:handleQuestionReport',@qb_root,112,NOW()),
('相似题运营统计','system:questionBank:questionPracticeStatistics',@qb_root,113,NOW());

SET @qb_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='questionBank' LIMIT 1);
SET @sp_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='similarPractice' LIMIT 1);

-- 管理菜单绑定前7项；学生菜单绑定知识点查询、推荐、作答、举报。
INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @qb_menu,`id`,CONCAT(@qb_root,'-',`id`),NOW() FROM `sys_resource`
WHERE `code` IN ('system:questionBank:questionBankPageList','system:questionBank:questionBankDetail',
'system:questionBank:saveQuestionBank','system:questionBank:deleteQuestionBank',
'system:questionBank:reviewQuestionBank','system:questionBank:knowledgePointList','system:questionBank:saveKnowledgePoint',
'system:questionBank:questionReportPageList','system:questionBank:handleQuestionReport',
'system:questionBank:questionPracticeStatistics');

INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @sp_menu,`id`,CONCAT(@qb_root,'-',`id`),NOW() FROM `sys_resource`
WHERE `code` IN ('system:questionBank:similarQuestionList','system:questionBank:submitQuestionPractice',
'system:questionBank:reportQuestion');

UPDATE `sys_menu` m SET
  m.`resource_ids`=(SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`),
  m.`resource_level`=(SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`)
WHERE m.`id` IN (@qb_menu,@sp_menu);

-- 已有错题本角色自动获得相似题练习菜单；已有API管理角色获得题库管理菜单。
SET @wrong_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='wrongQuestion' LIMIT 1);
SET @api_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='systemSetting-api' LIMIT 1);
INSERT IGNORE INTO `sys_role_menu` (`role_id`,`menu_id`,`create_time`)
SELECT `role_id`,@sp_menu,NOW() FROM `sys_role_menu` WHERE `menu_id`=@wrong_menu;
INSERT IGNORE INTO `sys_role_menu` (`role_id`,`menu_id`,`create_time`)
SELECT `role_id`,@qb_menu,NOW() FROM `sys_role_menu` WHERE `menu_id`=@api_menu;

-- 权限在登录时写入Redis；执行本脚本后请退出并重新登录。
