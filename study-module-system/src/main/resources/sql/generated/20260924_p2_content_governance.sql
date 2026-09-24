-- P2-4：统一内容审核、错误举报、版权凭证、授权到期与版本回退留痕。
-- 下架只阻止后续推荐；练习会话、作答与题目快照均不删除，保证历史可追溯。
CREATE TABLE IF NOT EXISTS `sys_question_content_governance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '治理记录ID',
  `question_id` bigint NOT NULL COMMENT '题库题目ID',
  `action` varchar(30) NOT NULL COMMENT 'DOWN下架、RESTORE恢复、LICENSE_UPDATE授权更新、RECORD_PROOF登记凭证、ROLLBACK回退',
  `issue_type` varchar(30) NOT NULL COMMENT 'STEM_ERROR、ANSWER_ERROR、ANALYSIS_ERROR、OUT_OF_SYLLABUS、DUPLICATE、INFRINGEMENT、LICENSE_EXPIRED',
  `handle_remark` varchar(500) DEFAULT NULL COMMENT '处理说明、下架原因或授权说明',
  `proof_file_ids` varchar(1000) DEFAULT NULL COMMENT 'questionBank上传类型的版权/授权凭证文件ID，逗号分隔',
  `license_version` varchar(50) DEFAULT NULL COMMENT '授权版本快照',
  `expire_at` date DEFAULT NULL COMMENT '授权到期日快照',
  `target_version_no` int DEFAULT NULL COMMENT '回退目标版本号',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_question_content_governance` (`question_id`,`create_time`),
  KEY `idx_question_content_action` (`action`,`issue_type`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目内容治理、版权凭证与回退记录';

SET @qb_root = (SELECT `id` FROM `sys_resource` WHERE `code`='system:questionBank' LIMIT 1);
INSERT IGNORE INTO `sys_resource` (`resource_name`,`code`,`pid`,`sort`,`create_time`) VALUES
('题目内容治理','system:questionBank:governQuestionContent',@qb_root,166,NOW()),
('回退题目版本','system:questionBank:rollbackQuestionBankVersion',@qb_root,167,NOW()),
('题目内容治理记录','system:questionBank:questionContentGovernanceList',@qb_root,168,NOW());

SET @qb_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='questionBank' LIMIT 1);
INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @qb_menu, `id`, CONCAT(@qb_root,'-',`id`), NOW() FROM `sys_resource`
WHERE `code` IN ('system:questionBank:governQuestionContent',
                 'system:questionBank:rollbackQuestionBankVersion',
                 'system:questionBank:questionContentGovernanceList');
UPDATE `sys_menu` m SET
  m.`resource_ids`=(SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`),
  m.`resource_level`=(SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`)
WHERE m.`id`=@qb_menu;
-- 权限在登录时载入缓存；执行本脚本后需重新登录。
