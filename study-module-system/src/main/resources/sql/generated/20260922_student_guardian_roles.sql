-- 为学生网页提供最小权限角色；系统管理菜单不授予学生或家长。
INSERT INTO `sys_role` (`role_name`, `menu_ids`, `menu_level`, `is_system`, `create_time`)
SELECT '智错本学生', '', '', 0, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_name` = '智错本学生');

INSERT INTO `sys_role` (`role_name`, `menu_ids`, `menu_level`, `is_system`, `create_time`)
SELECT '智错本家长', '', '', 0, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `role_name` = '智错本家长');

SET @study_student_role_id = (SELECT `id` FROM `sys_role` WHERE `role_name` = '智错本学生' LIMIT 1);
SET @study_guardian_role_id = (SELECT `id` FROM `sys_role` WHERE `role_name` = '智错本家长' LIMIT 1);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT @study_student_role_id, `id`, NOW() FROM `sys_menu`
WHERE `code` IN ('dashboard', 'wrongQuestion', 'wrongQuestion-create', 'wrongQuestion-detail',
                 'wrongQuestion-update', 'wrongQuestion-updateImage', 'wrongQuestion-import',
                 'wrongQuestion-export', 'wrongQuestion-delete', 'wrongQuestion-batchDelete',
                 'intelligentReview', 'intelligentReview-home', 'intelligentReview-today',
                 'intelligentReview-history', 'intelligentReview-setting', 'intelligentReview-practice',
                 'similarPractice', 'similarPracticeHistory', 'guardianCollaboration');

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT @study_guardian_role_id, `id`, NOW() FROM `sys_menu`
WHERE `code` = 'guardianCollaboration';
