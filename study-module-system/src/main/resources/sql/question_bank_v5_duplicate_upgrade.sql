-- 题库高相似检测、保存前确认与历史重复题清理权限（MySQL 8.0+，可重复执行）
SET @qb_root = (SELECT `id` FROM `sys_resource` WHERE `code`='system:questionBank' LIMIT 1);

INSERT IGNORE INTO `sys_resource` (`resource_name`,`code`,`pid`,`sort`,`create_time`) VALUES
('保存前重复题检测','system:questionBank:duplicateQuestionList',@qb_root,114,NOW()),
('历史重复题扫描','system:questionBank:duplicateQuestionHistory',@qb_root,115,NOW()),
('清理历史重复题','system:questionBank:cleanDuplicateQuestion',@qb_root,116,NOW());

SET @qb_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='questionBank' LIMIT 1);
INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @qb_menu,`id`,CONCAT(@qb_root,'-',`id`),NOW() FROM `sys_resource`
WHERE `code` IN ('system:questionBank:duplicateQuestionList',
                 'system:questionBank:duplicateQuestionHistory',
                 'system:questionBank:cleanDuplicateQuestion');

UPDATE `sys_menu` m SET
  m.`resource_ids`=(SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`),
  m.`resource_level`=(SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`)
WHERE m.`id`=@qb_menu;

-- 权限在登录时写入Redis；执行本脚本后请退出并重新登录。
