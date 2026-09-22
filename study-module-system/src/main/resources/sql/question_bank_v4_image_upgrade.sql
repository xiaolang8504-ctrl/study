-- 题库图片资源管理：文件关联、排序、封面和独立删除。
CREATE TABLE IF NOT EXISTS `sys_question_bank_image` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目图片关联ID',
  `question_id` bigint NOT NULL COMMENT '题库题目ID',
  `file_id` int DEFAULT NULL COMMENT 'sys_file文件ID',
  `image_url` varchar(1000) DEFAULT NULL COMMENT '兼容外部图片URL',
  `original_name` varchar(255) DEFAULT NULL COMMENT '图片原始文件名',
  `sort` int NOT NULL DEFAULT 1 COMMENT '图片展示顺序',
  `is_cover` tinyint NOT NULL DEFAULT 0 COMMENT '是否封面：0否，1是',
  PRIMARY KEY (`id`),
  KEY `idx_question_bank_image_question` (`question_id`,`sort`),
  KEY `idx_question_bank_image_file` (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题库题目图片文件关联';

-- 旧image_urls字段继续保留；题目下次编辑保存时自动迁移到本关联表。
