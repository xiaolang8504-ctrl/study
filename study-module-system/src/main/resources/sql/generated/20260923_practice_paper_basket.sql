-- 可编辑组卷篮：保存练习卷留白与答案排版设置。
ALTER TABLE `sys_practice_session`
    ADD COLUMN `blank_line_count` tinyint unsigned NOT NULL DEFAULT 3
        COMMENT '每题作答留白行数' AFTER `generation_reason`,
    ADD COLUMN `answer_position` varchar(20) NOT NULL DEFAULT 'END'
        COMMENT '答案位置：AFTER_EACH逐题显示，END卷末集中显示' AFTER `blank_line_count`;
