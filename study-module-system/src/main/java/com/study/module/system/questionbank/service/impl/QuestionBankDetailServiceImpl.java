package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.dto.response.QuestionBankDetailResp;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.mapper.QuestionBankMapper;
import com.study.module.system.questionbank.service.QuestionBankDetailService;
import com.study.module.system.questionbank.service.QuestionBankImageService;
import com.study.module.system.questionbank.service.QuestionKnowledgePointService;
import com.study.module.system.questionbank.convert.QuestionBankConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 题库题目详情服务实现
 */
@Service
public class QuestionBankDetailServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank>
        implements QuestionBankDetailService {

    @Autowired
    QuestionKnowledgePointService questionKnowledgePointService;

    @Autowired
    QuestionBankImageService questionBankImageService;

    /**
     * 查询题库题目详情
     */
    @Override
    public QuestionBankDetailResp questionBankDetail(Long id) {
        QuestionBank entity = getById(id);
        if (entity == null) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_NOT_EXIST);
        }
        QuestionBankDetailResp response = QuestionBankConvert.INSTANCE.toQuestionBankDetailResp(entity);
        questionKnowledgePointService.fill(response);
        response.setImages(questionBankImageService.imageList(id));
        return response;
    }
}
