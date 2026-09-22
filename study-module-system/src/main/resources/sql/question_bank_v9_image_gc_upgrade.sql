-- 题库图片管理与孤立文件回收菜单/权限，可重复执行。
INSERT IGNORE INTO `sys_menu`
(`menu_name`,`code`,`pid`,`level`,`sort`,`resource_ids`,`resource_level`,`create_time`)
VALUES ('图片管理','questionImage',0,1,7,'','',NOW());

INSERT IGNORE INTO `sys_resource`
(`resource_name`,`code`,`pid`,`sort`,`create_time`)
VALUES ('文件管理','system:file',0,130,NOW());
SET @file_root = (SELECT `id` FROM `sys_resource` WHERE `code`='system:file' LIMIT 1);

INSERT IGNORE INTO `sys_resource` (`resource_name`,`code`,`pid`,`sort`,`create_time`) VALUES
('题目图片分页列表','system:file:questionImagePageList',@file_root,131,NOW()),
('删除题目孤立图片','system:file:deleteOrphanQuestionImage',@file_root,132,NOW()),
('清理题目孤立图片','system:file:cleanOrphanQuestionImage',@file_root,133,NOW());

SET @image_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='questionImage' LIMIT 1);
INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @image_menu,`id`,CONCAT(@file_root,'-',`id`),NOW() FROM `sys_resource`
WHERE `code` IN ('system:file:questionImagePageList',
                 'system:file:deleteOrphanQuestionImage',
                 'system:file:cleanOrphanQuestionImage');

UPDATE `sys_menu` m SET
  m.`resource_ids`=(SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`),
  m.`resource_level`=(SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`)
WHERE m.`id`=@image_menu;

-- 已拥有题库管理菜单的角色自动获得图片管理菜单。
SET @qb_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='questionBank' LIMIT 1);
INSERT IGNORE INTO `sys_role_menu` (`role_id`,`menu_id`,`create_time`)
SELECT `role_id`,@image_menu,NOW() FROM `sys_role_menu` WHERE `menu_id`=@qb_menu;

-- 权限缓存仅在登录时构建；执行脚本后请退出并重新登录。
