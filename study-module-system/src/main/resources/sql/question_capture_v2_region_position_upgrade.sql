ALTER TABLE `sys_question_capture_region` ADD COLUMN `left_position` int NOT NULL DEFAULT 0 COMMENT '归一化左坐标0-10000';
ALTER TABLE `sys_question_capture_region` ADD COLUMN `top_position` int NOT NULL DEFAULT 0 COMMENT '归一化上坐标0-10000';
ALTER TABLE `sys_question_capture_region` ADD COLUMN `width` int NOT NULL DEFAULT 10000 COMMENT '归一化宽度0-10000';
ALTER TABLE `sys_question_capture_region` ADD COLUMN `height` int NOT NULL DEFAULT 10000 COMMENT '归一化高度0-10000';
