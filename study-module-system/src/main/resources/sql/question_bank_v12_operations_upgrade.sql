-- 批量审核、练习历史、人工合并与A/B实验后台，可重复执行。

INSERT IGNORE INTO `sys_menu`
(`menu_name`,`code`,`pid`,`level`,`sort`,`resource_ids`,`resource_level`,`create_time`) VALUES
('练习历史','similarPracticeHistory',0,1,8,'','',NOW()),
('A/B实验管理','questionExperiment',0,1,9,'','',NOW());

SET @qb_root = (SELECT `id` FROM `sys_resource` WHERE `code`='system:questionBank' LIMIT 1);
INSERT IGNORE INTO `sys_resource` (`resource_name`,`code`,`pid`,`sort`,`create_time`) VALUES
('批量审核题库题目','system:questionBank:batchReviewQuestionBank',@qb_root,129,NOW()),
('相似题练习历史明细','system:questionBank:questionPracticeHistoryPageList',@qb_root,130,NOW()),
('人工合并高相似题','system:questionBank:mergeDuplicateQuestion',@qb_root,131,NOW());

SET @qb_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='questionBank' LIMIT 1);
SET @sp_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='similarPractice' LIMIT 1);
SET @history_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='similarPracticeHistory' LIMIT 1);
SET @experiment_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='questionExperiment' LIMIT 1);

INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @qb_menu,`id`,CONCAT(@qb_root,'-',`id`),NOW() FROM `sys_resource`
WHERE `code` IN ('system:questionBank:batchReviewQuestionBank','system:questionBank:mergeDuplicateQuestion');
INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @history_menu,`id`,CONCAT(@qb_root,'-',`id`),NOW() FROM `sys_resource`
WHERE `code`='system:questionBank:questionPracticeHistoryPageList';
INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @experiment_menu,`id`,CONCAT(@qb_root,'-',`id`),NOW() FROM `sys_resource`
WHERE `code` IN ('system:questionBank:questionExperimentDetail','system:questionBank:saveQuestionExperiment','system:questionBank:questionExperimentHistory');

UPDATE `sys_menu` m SET
  m.`resource_ids`=(SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`),
  m.`resource_level`=(SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`)
WHERE m.`id` IN (@qb_menu,@history_menu,@experiment_menu);

INSERT IGNORE INTO `sys_role_menu` (`role_id`,`menu_id`,`create_time`)
SELECT `role_id`,@history_menu,NOW() FROM `sys_role_menu` WHERE `menu_id`=@sp_menu;
INSERT IGNORE INTO `sys_role_menu` (`role_id`,`menu_id`,`create_time`)
SELECT `role_id`,@experiment_menu,NOW() FROM `sys_role_menu` WHERE `menu_id`=@qb_menu;

-- 权限缓存仅在登录时构建；执行脚本后请退出并重新登录。
