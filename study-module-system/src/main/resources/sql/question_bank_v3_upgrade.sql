-- 题库相似题一期收尾：重复题、富内容、主观题自评、错题知识点ID化、A/B实验。
ALTER TABLE `sys_question_bank`
  ADD COLUMN `question_hash` varchar(32) DEFAULT NULL COMMENT '规范化题干MD5，用于重复题检测' AFTER `question_content`,
  ADD COLUMN `content_format` varchar(20) NOT NULL DEFAULT 'TEXT' COMMENT '内容格式：TEXT普通文本，LATEX公式' AFTER `question_hash`,
  ADD COLUMN `image_urls` text DEFAULT NULL COMMENT '题目图片URL，多个使用逗号分隔' AFTER `content_format`,
  ADD KEY `idx_question_bank_hash` (`question_hash`);

UPDATE `sys_question_bank`
SET `question_hash`=MD5(LOWER(REPLACE(REPLACE(REPLACE(CONCAT(`grade`,'|',`subject`,'|',`question_content`),' ',''),'，',''), '。','')))
WHERE `question_hash` IS NULL;

ALTER TABLE `sys_question_recommendation_log`
  ADD COLUMN `experiment_group` varchar(10) NOT NULL DEFAULT 'A' COMMENT '推荐A/B实验分组' AFTER `recommend_reason`,
  ADD COLUMN `judge_type` varchar(20) DEFAULT NULL COMMENT '判题方式：AUTO自动，SELF学生自评' AFTER `is_correct`,
  ADD KEY `idx_recommend_experiment` (`experiment_group`,`answer_time`,`is_correct`);

CREATE TABLE IF NOT EXISTS `sys_wrong_question_knowledge_point` (
  `wrong_question_id` bigint NOT NULL COMMENT '个人错题ID',
  `knowledge_point_id` bigint NOT NULL COMMENT '标准知识点ID',
  `relation_source` varchar(30) NOT NULL DEFAULT 'MANUAL' COMMENT '关联来源：MANUAL人工，AUTO_MIGRATION自动迁移',
  PRIMARY KEY (`wrong_question_id`,`knowledge_point_id`),
  KEY `idx_wrong_point_reverse` (`knowledge_point_id`,`wrong_question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='个人错题知识点标准关联';

-- 将历史错题learning_point文本中能匹配的标准知识点自动迁移为ID关系。
INSERT IGNORE INTO `sys_wrong_question_knowledge_point`
(`wrong_question_id`,`knowledge_point_id`,`relation_source`)
SELECT wq.`id`,kp.`id`,'AUTO_MIGRATION'
FROM `sys_wrong_question` wq
JOIN `sys_knowledge_point` kp
  ON kp.`grade`=wq.`grade` AND kp.`subject`=wq.`subject` AND kp.`enable`=1
 AND wq.`learning_point` IS NOT NULL AND wq.`learning_point` LIKE CONCAT('%',kp.`point_name`,'%');
