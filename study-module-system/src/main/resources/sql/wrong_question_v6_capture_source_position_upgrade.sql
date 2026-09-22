ALTER TABLE `sys_wrong_question`
    ADD COLUMN `capture_task_id` bigint DEFAULT NULL COMMENT '来源采集任务ID',
    ADD COLUMN `capture_page_id` bigint DEFAULT NULL COMMENT '来源采集页面ID',
    ADD COLUMN `capture_region_id` bigint DEFAULT NULL COMMENT '来源采集题块ID',
    ADD COLUMN `capture_source_page_no` int DEFAULT NULL COMMENT '原文件页码',
    ADD COLUMN `capture_left_position` int DEFAULT NULL COMMENT '题块归一化左坐标',
    ADD COLUMN `capture_top_position` int DEFAULT NULL COMMENT '题块归一化上坐标',
    ADD COLUMN `capture_width` int DEFAULT NULL COMMENT '题块归一化宽度',
    ADD COLUMN `capture_height` int DEFAULT NULL COMMENT '题块归一化高度';

CREATE INDEX `idx_wrong_question_capture_region` ON `sys_wrong_question` (`capture_region_id`);
