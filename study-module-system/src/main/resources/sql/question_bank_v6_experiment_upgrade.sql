-- 错题知识点人工维护与A/B实验管理升级（MySQL 8.0+，可重复执行）
CREATE TABLE IF NOT EXISTS `sys_question_experiment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '实验配置ID',
  `experiment_name` varchar(100) NOT NULL COMMENT '实验名称',
  `enable` tinyint NOT NULL DEFAULT 0 COMMENT '启用状态：0停用，1启用',
  `group_a_traffic` tinyint NOT NULL DEFAULT 50 COMMENT 'A组流量百分比，B组为100减A组',
  `start_time` datetime NOT NULL COMMENT '实验开始时间',
  `end_time` datetime NOT NULL COMMENT '实验结束时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '实验备注',
  `create_id` bigint NOT NULL COMMENT '创建人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_question_experiment_running` (`enable`,`start_time`,`end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='相似题推荐A/B实验配置';

CREATE TABLE IF NOT EXISTS `sys_question_experiment_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史ID',
  `experiment_id` bigint NOT NULL COMMENT '实验配置ID',
  `experiment_name` varchar(100) NOT NULL COMMENT '实验名称快照',
  `enable` tinyint NOT NULL COMMENT '启停快照',
  `group_a_traffic` tinyint NOT NULL COMMENT 'A组流量快照',
  `start_time` datetime NOT NULL COMMENT '开始时间快照',
  `end_time` datetime NOT NULL COMMENT '结束时间快照',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注快照',
  `operator_id` bigint NOT NULL COMMENT '操作人用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
  PRIMARY KEY (`id`),
  KEY `idx_question_experiment_history` (`experiment_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='A/B实验配置变更历史';

ALTER TABLE `sys_question_recommendation_log`
  ADD COLUMN `experiment_id` bigint DEFAULT NULL COMMENT 'A/B实验配置ID，旧数据为空' AFTER `experiment_group`,
  ADD KEY `idx_recommend_experiment_id` (`experiment_id`,`experiment_group`,`answer_time`);

SET @qb_root = (SELECT `id` FROM `sys_resource` WHERE `code`='system:questionBank' LIMIT 1);
INSERT IGNORE INTO `sys_resource` (`resource_name`,`code`,`pid`,`sort`,`create_time`) VALUES
('A/B实验配置详情','system:questionBank:questionExperimentDetail',@qb_root,117,NOW()),
('保存A/B实验配置','system:questionBank:saveQuestionExperiment',@qb_root,118,NOW()),
('A/B实验历史','system:questionBank:questionExperimentHistory',@qb_root,119,NOW());

SET @qb_menu = (SELECT `id` FROM `sys_menu` WHERE `code`='questionBank' LIMIT 1);
INSERT IGNORE INTO `sys_menu_resource` (`menu_id`,`resource_id`,`resource_level`,`create_time`)
SELECT @qb_menu,`id`,CONCAT(@qb_root,'-',`id`),NOW() FROM `sys_resource`
WHERE `code` IN ('system:questionBank:questionExperimentDetail',
                 'system:questionBank:saveQuestionExperiment',
                 'system:questionBank:questionExperimentHistory');

UPDATE `sys_menu` m SET
  m.`resource_ids`=(SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`),
  m.`resource_level`=(SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`) FROM `sys_menu_resource` mr WHERE mr.`menu_id`=m.`id`)
WHERE m.`id`=@qb_menu;

-- 资源权限登录时写入Redis；执行脚本后请退出并重新登录。
