-- 精品题库一期字段注释升级，适用于已执行 question_bank.sql 的数据库。
ALTER TABLE `sys_knowledge_point`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '知识点ID',
  MODIFY `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父知识点ID，0表示根节点',
  MODIFY `point_code` varchar(80) NOT NULL COMMENT '知识点唯一编码',
  MODIFY `point_name` varchar(100) NOT NULL COMMENT '知识点名称',
  MODIFY `grade` varchar(20) NOT NULL COMMENT '年级字典键值',
  MODIFY `subject` varchar(30) NOT NULL COMMENT '科目字典键值',
  MODIFY `sort` int NOT NULL DEFAULT 0 COMMENT '同级排序号',
  MODIFY `enable` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态：0停用，1启用',
  MODIFY `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE `sys_question_bank`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题库题目ID',
  MODIFY `grade` varchar(20) NOT NULL COMMENT '年级字典键值',
  MODIFY `grade_name` varchar(30) DEFAULT NULL COMMENT '年级名称',
  MODIFY `subject` varchar(30) NOT NULL COMMENT '科目字典键值',
  MODIFY `subject_name` varchar(30) DEFAULT NULL COMMENT '科目名称',
  MODIFY `question_type` varchar(30) NOT NULL COMMENT '题型字典键值',
  MODIFY `question_type_name` varchar(30) DEFAULT NULL COMMENT '题型名称',
  MODIFY `question_title` varchar(255) NOT NULL COMMENT '题目标题',
  MODIFY `question_content` text NOT NULL COMMENT '题干内容',
  MODIFY `options_json` text DEFAULT NULL COMMENT '选择题选项JSON',
  MODIFY `correct_answer` text NOT NULL COMMENT '标准答案',
  MODIFY `analysis` text DEFAULT NULL COMMENT '题目解析',
  MODIFY `difficulty` tinyint NOT NULL DEFAULT 3 COMMENT '难度等级：1简单至5困难',
  MODIFY `source_name` varchar(100) DEFAULT NULL COMMENT '题目来源名称',
  MODIFY `review_status` tinyint NOT NULL DEFAULT 0 COMMENT '审核状态：0待审核，1通过，2驳回',
  MODIFY `review_remark` varchar(500) DEFAULT NULL COMMENT '审核备注或驳回原因',
  MODIFY `reviewer_id` bigint DEFAULT NULL COMMENT '审核人用户ID',
  MODIFY `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  MODIFY `enable` tinyint NOT NULL DEFAULT 1 COMMENT '启用状态：0停用，1启用',
  MODIFY `create_id` bigint NOT NULL COMMENT '创建人用户ID',
  MODIFY `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  MODIFY `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

ALTER TABLE `sys_question_knowledge_point`
  MODIFY `question_id` bigint NOT NULL COMMENT '题库题目ID',
  MODIFY `knowledge_point_id` bigint NOT NULL COMMENT '知识点ID',
  MODIFY `is_primary` tinyint NOT NULL DEFAULT 0 COMMENT '是否主知识点：0否，1是';

ALTER TABLE `sys_question_recommendation_log`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '推荐记录ID',
  MODIFY `batch_no` varchar(40) NOT NULL COMMENT '单次推荐批次号',
  MODIFY `user_id` bigint NOT NULL COMMENT '学生用户ID',
  MODIFY `wrong_question_id` bigint NOT NULL COMMENT '推荐来源错题ID',
  MODIFY `bank_question_id` bigint NOT NULL COMMENT '被推荐的题库题目ID',
  MODIFY `rank_no` int NOT NULL COMMENT '题目在推荐批次中的排序',
  MODIFY `recommend_score` decimal(8,2) NOT NULL DEFAULT 0 COMMENT '推荐匹配分数',
  MODIFY `exposure_time` datetime NOT NULL COMMENT '题目曝光时间',
  MODIFY `student_answer` text DEFAULT NULL COMMENT '学生提交答案',
  MODIFY `is_correct` tinyint DEFAULT NULL COMMENT '是否回答正确：0错误，1正确，未作答为空',
  MODIFY `duration_seconds` int DEFAULT NULL COMMENT '本次作答耗时秒数',
  MODIFY `answer_time` datetime DEFAULT NULL COMMENT '答案提交时间',
  MODIFY `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间';

ALTER TABLE `sys_question_report`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报记录ID',
  MODIFY `user_id` bigint NOT NULL COMMENT '举报学生用户ID',
  MODIFY `bank_question_id` bigint NOT NULL COMMENT '被举报的题库题目ID',
  MODIFY `report_type` varchar(30) NOT NULL COMMENT '举报类型编码',
  MODIFY `report_content` varchar(500) DEFAULT NULL COMMENT '举报问题描述',
  MODIFY `status` tinyint NOT NULL DEFAULT 0 COMMENT '处理状态：0待处理，1已处理，2无效',
  MODIFY `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间';
