-- 知识点树维护、题目版本与审核历史，可重复执行。
CREATE TABLE IF NOT EXISTS `sys_question_bank_version` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  `question_id` bigint NOT NULL COMMENT '题库题目ID',
  `version_no` int NOT NULL COMMENT '版本号',
  `operation_type` varchar(20) NOT NULL COMMENT 'CREATE创建、UPDATE更新',
  `snapshot_json` longtext NOT NULL COMMENT '题目完整编辑快照',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '保存时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_question_version` (`question_id`,`version_no`),
  KEY `idx_question_version_time` (`question_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题库题目版本快照';

CREATE TABLE IF NOT EXISTS `sys_question_bank_review_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审核日志ID',
  `question_id` bigint NOT NULL COMMENT '题库题目ID',
  `review_status` tinyint NOT NULL COMMENT '审核状态：1通过，2驳回',
  `review_remark` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `reviewer_id` bigint NOT NULL COMMENT '审核人ID',
  `review_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  PRIMARY KEY (`id`),
  KEY `idx_question_review_time` (`question_id`,`review_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题库题目审核历史';

SET @qb_root = (SELECT `id` FROM `sys_resource` WHERE `code`='system:questionBank' LIMIT 1);
INSERT IGNORE INTO `sys_resource` (`resource_name`,`code`,`pid`,`sort`,`create_time`) VALUES
('知识点树','system:questionBank:knowledgePointTree',@qb_root,124,NOW()),
('删除知识点','system:questionBank:deleteKnowledgePoint',@qb_root,125,NOW()),
('题目版本列表','system:questionBank:questionBankVersionList',@qb_root,126,NOW()),
('题目审核历史','system:questionBank:questionBankReviewHistory',@qb_root,127,NOW());

SET @qb_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='questionBank' LIMIT 1);
INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @qb_menu,`id`,CONCAT(@qb_root,'-',`id`),NOW() FROM `sys_resource`
WHERE `code` IN ('system:questionBank:knowledgePointTree',
                 'system:questionBank:deleteKnowledgePoint',
                 'system:questionBank:questionBankVersionList',
                 'system:questionBank:questionBankReviewHistory');

UPDATE `sys_menu` m SET
  m.`resource_ids`=(SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`),
  m.`resource_level`=(SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`)
WHERE m.`id`=@qb_menu;

-- 权限缓存仅在登录时构建；执行脚本后请退出并重新登录。
