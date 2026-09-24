-- P2-5：多孩子独立周计划、周报退订，以及学生确认后才生效的待办建议。
CREATE TABLE IF NOT EXISTS `guardian_weekly_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '周计划ID',
  `guardian_user_id` bigint NOT NULL COMMENT '家长用户ID',
  `student_user_id` bigint NOT NULL COMMENT '学生用户ID',
  `week_start_date` date NOT NULL COMMENT '计划周的周一日期',
  `plan_title` varchar(120) NOT NULL COMMENT '计划标题',
  `plan_content` varchar(2000) NOT NULL COMMENT '计划沟通内容',
  `target_review_count` int NOT NULL DEFAULT 0 COMMENT '本周建议有效复习次数',
  `report_suggestion_snapshot` varchar(1000) DEFAULT NULL COMMENT '生成计划时的周报建议快照',
  `todo_status` tinyint NOT NULL DEFAULT 0 COMMENT '0未请求、1待学生确认、2学生确认、3学生暂不接受',
  `student_confirm_user_id` bigint DEFAULT NULL COMMENT '确认学生ID',
  `todo_request_time` datetime DEFAULT NULL COMMENT '请求转待办时间',
  `student_confirm_time` datetime DEFAULT NULL COMMENT '学生确认或拒绝时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_guardian_student_week_plan` (`guardian_user_id`,`student_user_id`,`week_start_date`),
  KEY `idx_weekly_plan_student_todo` (`student_user_id`,`todo_status`,`week_start_date`),
  KEY `idx_weekly_plan_guardian_student` (`guardian_user_id`,`student_user_id`,`week_start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家长与学生独立周计划';

SET @guardian_root = (SELECT `id` FROM `sys_resource` WHERE `code`='system:guardian' LIMIT 1);
INSERT IGNORE INTO `sys_resource` (`resource_name`,`code`,`pid`,`sort`,`create_time`) VALUES
('家庭协作','system:guardian',0,180,NOW());
SET @guardian_root = (SELECT `id` FROM `sys_resource` WHERE `code`='system:guardian' LIMIT 1);
INSERT IGNORE INTO `sys_resource` (`resource_name`,`code`,`pid`,`sort`,`create_time`) VALUES
('保存家长周计划','system:guardian:saveGuardianWeeklyPlan',@guardian_root,181,NOW()),
('请求学生确认周计划待办','system:guardian:requestGuardianWeeklyPlanTodo',@guardian_root,182,NOW()),
('学生确认家长周计划待办','system:guardian:confirmGuardianWeeklyPlanTodo',@guardian_root,183,NOW()),
('家长周计划列表','system:guardian:guardianWeeklyPlanList',@guardian_root,184,NOW()),
('退订家长周报提醒','system:guardian:unsubscribeGuardianWeeklyReport',@guardian_root,185,NOW());

SET @guardian_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='guardianCollaboration' LIMIT 1);
INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @guardian_menu, `id`, CONCAT(@guardian_root,'-',`id`), NOW() FROM `sys_resource`
WHERE `code` IN ('system:guardian:saveGuardianWeeklyPlan',
                 'system:guardian:requestGuardianWeeklyPlanTodo',
                 'system:guardian:confirmGuardianWeeklyPlanTodo',
                 'system:guardian:guardianWeeklyPlanList',
                 'system:guardian:unsubscribeGuardianWeeklyReport');
UPDATE `sys_menu` m SET
  m.`resource_ids`=(SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`),
  m.`resource_level`=(SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`)
WHERE m.`id`=@guardian_menu;
-- 权限缓存仅在登录时构建；执行脚本后需重新登录。
