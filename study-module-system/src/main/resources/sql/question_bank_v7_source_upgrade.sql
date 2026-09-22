-- 题库来源字典键值升级（MySQL 8.0+）
ALTER TABLE `sys_question_bank`
  ADD COLUMN `source` varchar(30) DEFAULT NULL COMMENT '题目来源字典键值' AFTER `difficulty`;

-- 尝试用历史来源名称匹配字典标签，无法匹配的数据保留为空，需编辑后选择标准来源。
UPDATE `sys_question_bank` q
JOIN `sys_dict_data` d ON d.`dict_type`='source' AND d.`dict_label`=q.`source_name`
SET q.`source`=d.`dict_value`
WHERE q.`source` IS NULL;
