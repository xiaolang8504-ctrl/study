-- 题库相似题一期增强：推荐算法可解释性、举报处理闭环、统计接口和权限。
ALTER TABLE `sys_question_recommendation_log`
  ADD COLUMN `algorithm_version` varchar(30) NOT NULL DEFAULT 'SIMILAR_V2' COMMENT '推荐算法版本' AFTER `recommend_score`,
  ADD COLUMN `match_type` varchar(30) NOT NULL DEFAULT 'STRUCTURED' COMMENT '候选召回方式' AFTER `algorithm_version`,
  ADD COLUMN `recommend_reason` varchar(255) DEFAULT NULL COMMENT '面向学生的推荐原因' AFTER `match_type`,
  ADD KEY `idx_recommend_recent_exposure` (`user_id`,`bank_question_id`,`exposure_time`);

ALTER TABLE `sys_question_report`
  ADD COLUMN `handler_id` bigint DEFAULT NULL COMMENT '处理人用户ID' AFTER `status`,
  ADD COLUMN `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理备注' AFTER `handler_id`,
  ADD COLUMN `handle_time` datetime DEFAULT NULL COMMENT '处理时间' AFTER `handle_remark`;

SET @qb_root = (SELECT `id` FROM `sys_resource` WHERE `code`='system:questionBank' LIMIT 1);
SET @qb_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='questionBank' LIMIT 1);

INSERT IGNORE INTO `sys_resource` (`resource_name`,`code`,`pid`,`sort`,`create_time`) VALUES
('题目举报分页列表','system:questionBank:questionReportPageList',@qb_root,111,NOW()),
('处理题目举报','system:questionBank:handleQuestionReport',@qb_root,112,NOW()),
('相似题运营统计','system:questionBank:questionPracticeStatistics',@qb_root,113,NOW());

INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @qb_menu,`id`,CONCAT(@qb_root,'-',`id`),NOW() FROM `sys_resource`
WHERE `code` IN ('system:questionBank:questionReportPageList',
                 'system:questionBank:handleQuestionReport',
                 'system:questionBank:questionPracticeStatistics');

UPDATE `sys_menu` m SET
  m.`resource_ids`=(SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`),
  m.`resource_level`=(SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`)
WHERE m.`id`=@qb_menu;

-- 执行后请重新登录，刷新Redis中的接口权限。
