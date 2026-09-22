-- 主观题答案解锁、自评、申诉及教师复核升级（MySQL 8.0+）
ALTER TABLE `sys_question_bank`
  ADD COLUMN `judge_mode` varchar(20) NOT NULL DEFAULT 'AUTO' COMMENT '判题模式：AUTO自动，SELF查看答案后自评' AFTER `correct_answer`;

UPDATE `sys_question_bank`
SET `judge_mode`=CASE
  WHEN `question_type_name` REGEXP '简答|解答|作文|论述|计算过程|证明' THEN 'SELF'
  ELSE 'AUTO' END;

ALTER TABLE `sys_question_recommendation_log`
  ADD COLUMN `answer_viewed_time` datetime DEFAULT NULL COMMENT '主观题查看标准答案时间' AFTER `duration_seconds`;

CREATE TABLE IF NOT EXISTS `sys_question_practice_appeal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申诉ID',
  `recommendation_id` bigint NOT NULL COMMENT '推荐作答记录ID',
  `user_id` bigint NOT NULL COMMENT '申诉学生ID',
  `bank_question_id` bigint NOT NULL COMMENT '题库题目ID',
  `appeal_reason` varchar(500) NOT NULL COMMENT '申诉原因',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0待复核，1已复核',
  `review_correct` tinyint DEFAULT NULL COMMENT '教师最终判定：0错误，1正确',
  `review_remark` varchar(500) DEFAULT NULL COMMENT '教师复核说明',
  `reviewer_id` bigint DEFAULT NULL COMMENT '复核教师ID',
  `review_time` datetime DEFAULT NULL COMMENT '复核时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申诉时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_practice_appeal_log` (`recommendation_id`),
  KEY `idx_question_practice_appeal_status` (`status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='主观题作答申诉与教师复核';

SET @qb_root = (SELECT `id` FROM `sys_resource` WHERE `code`='system:questionBank' LIMIT 1);
INSERT IGNORE INTO `sys_resource` (`resource_name`,`code`,`pid`,`sort`,`create_time`) VALUES
('主观题查看标准答案','system:questionBank:viewQuestionPracticeAnswer',@qb_root,120,NOW()),
('提交主观题作答申诉','system:questionBank:createQuestionPracticeAppeal',@qb_root,121,NOW()),
('主观题作答申诉列表','system:questionBank:questionPracticeAppealList',@qb_root,122,NOW()),
('教师复核主观题申诉','system:questionBank:reviewQuestionPracticeAppeal',@qb_root,123,NOW());

SET @qb_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='questionBank' LIMIT 1);
SET @sp_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='similarPractice' LIMIT 1);
INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @qb_menu,`id`,CONCAT(@qb_root,'-',`id`),NOW() FROM `sys_resource`
WHERE `code` IN ('system:questionBank:questionPracticeAppealList','system:questionBank:reviewQuestionPracticeAppeal');
INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @sp_menu,`id`,CONCAT(@qb_root,'-',`id`),NOW() FROM `sys_resource`
WHERE `code` IN ('system:questionBank:viewQuestionPracticeAnswer','system:questionBank:createQuestionPracticeAppeal');

UPDATE `sys_menu` m SET
  m.`resource_ids`=(SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`),
  m.`resource_level`=(SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`)
WHERE m.`id` IN (@qb_menu,@sp_menu);

-- 权限写入Redis，执行后需重新登录。
