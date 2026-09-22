-- P1：学习偏好（教材）与个人数据备份权限。
-- 适用数据库：已执行 review.sql 的 MySQL 8.0+ 环境。

CREATE TABLE IF NOT EXISTS `sys_learning_profile` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学习偏好ID',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `grade` varchar(32) DEFAULT NULL COMMENT '年级字典键值',
    `subject` varchar(32) DEFAULT NULL COMMENT '科目字典键值',
    `book_id` bigint DEFAULT NULL COMMENT '当前教材ID',
    `book_title` varchar(255) DEFAULT NULL COMMENT '教材标题快照',
    `create_time` datetime NOT NULL COMMENT '创建时间',
    `update_time` datetime NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_learning_profile_user` (`user_id`),
    KEY `idx_learning_profile_book` (`book_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='学生学习偏好表';

-- 菜单资源较多时避免刷新资源串发生 GROUP_CONCAT 截断。
SET SESSION group_concat_max_len = 8192;
SET @review_resource_id = (SELECT `id` FROM `sys_resource` WHERE `code` = 'system:review' LIMIT 1);
SET @review_setting_menu_id = (SELECT `id` FROM `sys_menu` WHERE `code` = 'intelligentReview-setting' LIMIT 1);
SET @review_practice_menu_id = (SELECT `id` FROM `sys_menu` WHERE `code` = 'intelligentReview-practice' LIMIT 1);

INSERT IGNORE INTO `sys_resource`
(`resource_name`, `code`, `pid`, `sort`, `create_time`)
VALUES ('查询学习偏好', 'system:review:learningProfile', @review_resource_id, 150, NOW()),
       ('更新学习偏好', 'system:review:updateLearningProfile', @review_resource_id, 151, NOW()),
       ('导出个人学习数据备份', 'system:review:learningDataBackup', @review_resource_id, 152, NOW()),
       ('个人练习卷导出详情', 'system:review:practicePaperDetail', @review_resource_id, 153, NOW());

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
SELECT @review_setting_menu_id, `id`, CONCAT(@review_resource_id, '-', `id`), NOW()
FROM `sys_resource`
WHERE `code` IN ('system:review:learningProfile', 'system:review:updateLearningProfile',
                 'system:review:learningDataBackup');

INSERT IGNORE INTO `sys_menu_resource`
(`menu_id`, `resource_id`, `resource_level`, `create_time`)
SELECT @review_practice_menu_id, `id`, CONCAT(@review_resource_id, '-', `id`), NOW()
FROM `sys_resource`
WHERE `code` = 'system:review:practicePaperDetail';

UPDATE `sys_menu` m
SET m.`resource_ids` = (
        SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`)
        FROM `sys_menu_resource` mr WHERE mr.`menu_id` = m.`id`
    ),
    m.`resource_level` = (
        SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`)
        FROM `sys_menu_resource` mr WHERE mr.`menu_id` = m.`id`
    )
WHERE m.`id` = @review_setting_menu_id;

UPDATE `sys_menu` m
SET m.`resource_ids` = (
        SELECT GROUP_CONCAT(mr.`resource_id` ORDER BY mr.`resource_id`)
        FROM `sys_menu_resource` mr WHERE mr.`menu_id` = m.`id`
    ),
    m.`resource_level` = (
        SELECT GROUP_CONCAT(mr.`resource_level` ORDER BY mr.`resource_id`)
        FROM `sys_menu_resource` mr WHERE mr.`menu_id` = m.`id`
    )
WHERE m.`id` = @review_practice_menu_id;
