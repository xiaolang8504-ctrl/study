package com.study.module.system.questionbank.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 题目与知识点多对多关联。
 */
@Data
@TableName("sys_question_knowledge_point")
public class QuestionKnowledgePoint {
    /**
     * 题库题目ID。
     */
    private Long questionId;

    /**
     * 知识点ID。
     */
    private Long knowledgePointId;

    /**
     * 是否主知识点：0否，1是。
     */
    private Integer isPrimary;
}
