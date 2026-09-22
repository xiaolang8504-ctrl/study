-- 课本管理模块
-- 适用数据库：MySQL 8.0+

DROP TABLE IF EXISTS `book`;

CREATE TABLE `book` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课本ID',
    `grade` varchar(20) NOT NULL COMMENT '年级字典键值',
    `grade_name` varchar(20) NOT NULL COMMENT '年级字典名称',
    `subject` varchar(30) NOT NULL COMMENT '科目字典键值',
    `subject_name` varchar(30) NOT NULL COMMENT '科目字典名称',
    `title` varchar(255) NOT NULL COMMENT '课本标题',
    `content` text NOT NULL COMMENT '内容简介',
    `image_url` varchar(255) DEFAULT NULL COMMENT '课本附件文件ID或地址',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_book_grade_subject` (`grade`, `subject`),
    KEY `idx_book_create_user_id` (`create_user_id`),
    KEY `idx_book_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='课本表';
