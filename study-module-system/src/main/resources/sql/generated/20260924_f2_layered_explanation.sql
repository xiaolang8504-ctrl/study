-- F2-01：将提示、步骤和易错点作为题目可编辑内容保存；旧题继续回退到原有解析与错因。
ALTER TABLE `sys_wrong_question`
    ADD COLUMN `key_hint` text DEFAULT NULL COMMENT '解题关键提示，只给出思考方向' AFTER `analysis`,
    ADD COLUMN `solution_steps` text DEFAULT NULL COMMENT '分步解题过程' AFTER `key_hint`,
    ADD COLUMN `common_mistake` text DEFAULT NULL COMMENT '本题常见易错点' AFTER `solution_steps`;
