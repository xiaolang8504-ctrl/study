-- 组卷篮与高质量导出衔接：保存整份练习卷的题面图片选择。
ALTER TABLE `sys_practice_session`
    ADD COLUMN `image_mode` varchar(20) NOT NULL DEFAULT 'ORIGINAL'
        COMMENT '题面图片：ORIGINAL原图，GRAYSCALE灰度预览，TEXT_ONLY仅文字'
        AFTER `answer_position`;
