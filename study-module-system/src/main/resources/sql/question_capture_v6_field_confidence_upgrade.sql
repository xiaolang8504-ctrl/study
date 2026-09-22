ALTER TABLE `sys_question_capture_region`
    ADD COLUMN `question_title_confidence` int NOT NULL DEFAULT 0 COMMENT '题目标题OCR置信度',
    ADD COLUMN `question_content_confidence` int NOT NULL DEFAULT 0 COMMENT '题干OCR置信度',
    ADD COLUMN `wrong_answer_confidence` int NOT NULL DEFAULT 0 COMMENT '错误答案OCR置信度',
    ADD COLUMN `correct_answer_confidence` int NOT NULL DEFAULT 0 COMMENT '正确答案OCR置信度',
    ADD COLUMN `analysis_confidence` int NOT NULL DEFAULT 0 COMMENT '解析OCR置信度';
