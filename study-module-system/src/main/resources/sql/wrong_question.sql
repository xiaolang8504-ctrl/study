-- 初中生错题归档管理模块
-- 适用数据库：MySQL 8.0+

DROP TABLE IF EXISTS `wrong_question`;

CREATE TABLE `wrong_question` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '错题ID',
    `grade` varchar(20) NOT NULL COMMENT '年级字典键值',
    `grade_name` varchar(20) NOT NULL COMMENT '年级名称',
    `subject` varchar(30) NOT NULL COMMENT '科目字典键值',
    `subject_name` varchar(30) NOT NULL COMMENT '科目名称',
    `question_type` varchar(30) NOT NULL COMMENT '题目类型字典键值',
    `question_type_name` varchar(30) NOT NULL COMMENT '题目类型名称',
    `question_title` varchar(255) NOT NULL COMMENT '题目标题',
    `question_content` text NOT NULL COMMENT '题目内容',
    `wrong_answer` text DEFAULT NULL COMMENT '错误答案',
    `correct_answer` text DEFAULT NULL COMMENT '正确答案',
    `wrong_reason` text DEFAULT NULL COMMENT '错误原因',
    `analysis` text DEFAULT NULL COMMENT '题目解析',
    `learning_point` varchar(255) DEFAULT NULL COMMENT '知识点',
    `image_url` varchar(255) DEFAULT NULL COMMENT '默认题目图片，选择题时为A选项图片文件ID或地址',
    `image_url2` varchar(255) DEFAULT NULL COMMENT 'B选项图片文件ID或地址',
    `image_url3` varchar(255) DEFAULT NULL COMMENT 'C选项图片文件ID或地址',
    `image_url4` varchar(255) DEFAULT NULL COMMENT 'D选项图片文件ID或地址',
    `capture_task_id` bigint DEFAULT NULL COMMENT '来源采集任务ID',
    `capture_page_id` bigint DEFAULT NULL COMMENT '来源采集页面ID',
    `capture_region_id` bigint DEFAULT NULL COMMENT '来源采集题块ID',
    `capture_source_page_no` int DEFAULT NULL COMMENT '原文件页码',
    `capture_left_position` int DEFAULT NULL COMMENT '题块归一化左坐标',
    `capture_top_position` int DEFAULT NULL COMMENT '题块归一化上坐标',
    `capture_width` int DEFAULT NULL COMMENT '题块归一化宽度',
    `capture_height` int DEFAULT NULL COMMENT '题块归一化高度',
    `archive_status` tinyint NOT NULL DEFAULT 0 COMMENT '归档状态：0未归档，1已归档',
    `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_wrong_question_grade_subject` (`grade`, `subject`),
    KEY `idx_wrong_question_question_type` (`question_type`),
    KEY `idx_wrong_question_archive_status` (`archive_status`),
    KEY `idx_wrong_question_create_user_id` (`create_user_id`),
    KEY `idx_wrong_question_capture_region` (`capture_region_id`),
    KEY `idx_wrong_question_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='初中生错题归档表';

-- 飞桨 Studio PaddleOCR 配置示例，请写入 Nacos 对应 system-service 配置。
-- wrong-question:
--   ocr:
--     paddle:
--       enabled: true
--       job-url: https://paddleocr.aistudio-app.com/api/v2/ocr/jobs
--       token: 你的飞桨StudioToken
--       model: PaddleOCR-VL-1.6
--       upload-type: wrongQuestion
--       use-doc-orientation-classify: false
--       use-doc-unwarping: false
--       use-chart-recognition: false
--       poll-interval: 5000
--       max-poll-times: 60
