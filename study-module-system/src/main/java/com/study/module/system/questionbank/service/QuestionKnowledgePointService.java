package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.response.QuestionBankDetailResp;
import com.study.module.system.questionbank.dto.response.QuestionBankPageListResp;
import com.study.module.system.questionbank.entity.QuestionKnowledgePoint;

import java.util.List;

public interface QuestionKnowledgePointService extends IService<QuestionKnowledgePoint> {
    /**
     * 重写题目与知识点的关联
     */
    void rewrite(Long questionId, List<Long> pointIds);

    /**
     * 填充题目的知识点信息
     */
    void fill(QuestionBankDetailResp response);

    /**
     * 填充题目分页列表的知识点信息
     */
    void fill(QuestionBankPageListResp response);

    /**
     * 查询题目关联的知识点ID列表
     */
    List<Long> pointIds(Long questionId);

    /**
     * 查询题目关联的知识点名称列表
     */
    List<String> pointNames(Long questionId);
}
