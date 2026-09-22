package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.entity.KnowledgePoint;
import com.study.module.system.questionbank.entity.QuestionKnowledgePoint;
import com.study.module.system.questionbank.entity.WrongQuestionKnowledgePoint;
import com.study.module.system.questionbank.service.KnowledgePointDeleteService;
import com.study.module.system.questionbank.service.KnowledgePointService;
import com.study.module.system.questionbank.service.QuestionKnowledgePointService;
import com.study.module.system.questionbank.service.WrongQuestionKnowledgePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 知识点删除服务实现。
 */
@Service
public class KnowledgePointDeleteServiceImpl implements KnowledgePointDeleteService {

    @Autowired
    KnowledgePointService knowledgePointService;

    @Autowired
    QuestionKnowledgePointService questionKnowledgePointService;

    @Autowired
    WrongQuestionKnowledgePointService wrongQuestionKnowledgePointService;

    /**
     * 删除知识点。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteKnowledgePoint(Long id) {
        if (knowledgePointService.getById(id) == null) {
            throw new LogicException(ErrorCodeConstants.KNOWLEDGE_POINT_NOT_EXIST);
        }
        if (knowledgePointService.count(new LambdaQueryWrapper<KnowledgePoint>()
                .eq(KnowledgePoint::getParentId, id)) > 0) {
            throw new LogicException(ErrorCodeConstants.KNOWLEDGE_POINT_HAS_CHILDREN);
        }
        if (questionKnowledgePointService.count(new LambdaQueryWrapper<QuestionKnowledgePoint>()
                .eq(QuestionKnowledgePoint::getKnowledgePointId, id)) > 0
                || wrongQuestionKnowledgePointService.count(new LambdaQueryWrapper<WrongQuestionKnowledgePoint>()
                .eq(WrongQuestionKnowledgePoint::getKnowledgePointId, id)) > 0) {
            throw new LogicException(ErrorCodeConstants.KNOWLEDGE_POINT_IN_USE);
        }
        if (!knowledgePointService.removeById(id)) {
            throw new LogicException(ErrorCodeConstants.KNOWLEDGE_POINT_DELETE_FAIL);
        }
    }
}
