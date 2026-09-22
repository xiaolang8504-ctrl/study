-- 文件服务
-- 适用数据库：MySQL 8.0+

DROP TABLE IF EXISTS `file`;

CREATE TABLE `file` (
    `id` int NOT NULL AUTO_INCREMENT COMMENT '文件ID',
    `upload_type` varchar(50) NOT NULL COMMENT '上传类型',
    `origin_name` varchar(200) NOT NULL COMMENT '文件原名称',
    `save_name` varchar(255) NOT NULL COMMENT '文件保存新名称',
    `file_path` varchar(500) NOT NULL COMMENT '文件路径',
    `file_extension` varchar(20) NOT NULL COMMENT '文件扩展名称',
    `file_bytes` int NOT NULL COMMENT '文件字节数',
    `file_size` varchar(20) NOT NULL COMMENT '格式化后的文件大小',
    `create_id` bigint DEFAULT NULL COMMENT '上传用户ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    PRIMARY KEY (`id`),
    KEY `idx_file_upload_type` (`upload_type`),
    KEY `idx_file_create_id` (`create_id`),
    KEY `idx_file_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='文件表';
