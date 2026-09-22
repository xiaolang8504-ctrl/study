package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.entity.WrongQuestionKnowledgePoint;
import com.study.module.system.wrongquestion.entity.WrongQuestion;

import java.util.List;

public interface WrongQuestionKnowledgePointService extends IService<WrongQuestionKnowledgePoint> {
    /**
     * 解析错题关联的标准知识点ID列表
     */
    List<Long> resolvePointIds(WrongQuestion wrongQuestion);

    /**
     * 人工选择后立即全量重建标准知识点关系。
     */
    void rewrite(Long wrongQuestionId, List<Long> knowledgePointIds);
}
