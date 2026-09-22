-- 家庭协作入口仅控制前端可见性，实际数据访问继续由绑定关系校验。
INSERT IGNORE INTO `sys_menu`
(`menu_name`, `code`, `pid`, `level`, `sort`, `resource_ids`, `resource_level`, `create_time`)
VALUES ('家庭协作', 'guardianCollaboration', 0, 1, 8, '', '', NOW());

SET @guardian_menu_id = (SELECT `id` FROM `sys_menu` WHERE `code` = 'guardianCollaboration' LIMIT 1);

-- 学生和家长均可使用邀请与绑定入口；数据读取仍要求有效绑定。
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT `id`, @guardian_menu_id, NOW() FROM `sys_role`;
