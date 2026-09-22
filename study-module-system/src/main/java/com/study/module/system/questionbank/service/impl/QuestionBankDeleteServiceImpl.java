package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.entity.QuestionBankImage;
import com.study.module.system.questionbank.entity.QuestionKnowledgePoint;
import com.study.module.system.questionbank.mapper.QuestionBankMapper;
import com.study.module.system.questionbank.service.QuestionBankDeleteService;
import com.study.module.system.questionbank.service.QuestionBankImageService;
import com.study.module.system.questionbank.service.QuestionKnowledgePointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 题库题目删除服务实现
 */
@Service
public class QuestionBankDeleteServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank>
        implements QuestionBankDeleteService {

    @Autowired
    QuestionKnowledgePointService questionKnowledgePointService;

    @Autowired
    QuestionBankImageService questionBankImageService;

    /**
     * 删除题库题目
    */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestionBank(Long id) {
        if (getById(id) == null) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_NOT_EXIST);
        }
        questionKnowledgePointService.remove(new LambdaQueryWrapper<QuestionKnowledgePoint>()
                .eq(QuestionKnowledgePoint::getQuestionId, id));
        questionBankImageService.remove(new LambdaQueryWrapper<QuestionBankImage>()
                .eq(QuestionBankImage::getQuestionId, id));
        if (!removeById(id)) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_DELETE_FAIL);
        }
    }
}
