package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.questionbank.dto.request.QuestionBankPageListReq;
import com.study.module.system.questionbank.dto.response.QuestionBankPageListResp;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.mapper.QuestionBankMapper;
import com.study.module.system.questionbank.service.QuestionBankListService;
import com.study.module.system.questionbank.service.QuestionBankImageService;
import com.study.module.system.questionbank.service.QuestionKnowledgePointService;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import com.study.module.system.questionbank.convert.QuestionBankConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


/**
 * 题库题目列表服务实现
 */
@Service
public class QuestionBankListServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank>
        implements QuestionBankListService {

    @Autowired
    QuestionKnowledgePointService questionKnowledgePointService;

    @Autowired
    QuestionBankImageService questionBankImageService;

    /**
     * 分页查询题库题目
     */
    @Override
    public PageResult<QuestionBankPageListResp> questionBankPageList(QuestionBankPageListReq request) {
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(request.getGrade()), QuestionBank::getGrade, request.getGrade())
                .eq(StringUtils.hasText(request.getSubject()), QuestionBank::getSubject, request.getSubject())
                .eq(StringUtils.hasText(request.getQuestionType()), QuestionBank::getQuestionType, request.getQuestionType())
                .eq(request.getReviewStatus() != null, QuestionBank::getReviewStatus, request.getReviewStatus())
                .eq(request.getEnable() != null, QuestionBank::getEnable, request.getEnable());
        if (StringUtils.hasText(request.getKeyWord())) {
            wrapper.and(item -> item.like(QuestionBank::getQuestionTitle, request.getKeyWord())
                    .or().like(QuestionBank::getQuestionContent, request.getKeyWord()));
        }
        wrapper.orderByAsc(QuestionBank::getReviewStatus).orderByDesc(QuestionBank::getId);
        Page<QuestionBank> page = new Page<>(request.getCurrent(), request.getPageSize());
        page(page, wrapper);
        PageResult<QuestionBankPageListResp> result = PageUtils.wrap(page,
                QuestionBankConvert.INSTANCE::toQuestionBankPageListResp);
        result.getList().forEach(response -> {
            questionKnowledgePointService.fill(response);
            response.setImages(questionBankImageService.imageList(response.getId()));
        });
        return result;
    }
}
