package com.study.module.system.questionbank.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 个人错题与标准知识点关联。
 */
@Data
@TableName("sys_wrong_question_knowledge_point")
public class WrongQuestionKnowledgePoint {
    /**
     * 个人错题ID。
     */
    private Long wrongQuestionId;

    /**
     * 标准知识点ID。
     */
    private Long knowledgePointId;

    /**
     * 关联来源：MANUAL人工，AUTO_MIGRATION自动迁移。
     */
    private String relationSource;
}
