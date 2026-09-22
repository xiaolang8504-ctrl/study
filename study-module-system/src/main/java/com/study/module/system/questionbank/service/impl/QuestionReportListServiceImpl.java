package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.questionbank.dto.request.QuestionReportPageListReq;
import com.study.module.system.questionbank.dto.response.QuestionReportPageListResp;
import com.study.module.system.questionbank.entity.QuestionReport;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.mapper.QuestionReportMapper;
import com.study.module.system.questionbank.service.QuestionReportListService;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import com.study.module.system.questionbank.convert.QuestionReportConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.stream.Collectors;

/**
 * 题目举报列表服务实现
 */
@Service
public class QuestionReportListServiceImpl extends ServiceImpl<QuestionReportMapper, QuestionReport>
        implements QuestionReportListService {

    @Autowired
    QuestionBankService questionBankService;

    /**
     * 分页查询题目举报
     */
    @Override
    public PageResult<QuestionReportPageListResp> questionReportPageList(QuestionReportPageListReq request) {
        LambdaQueryWrapper<QuestionReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(request.getStatus() != null, QuestionReport::getStatus, request.getStatus())
                .eq(StringUtils.hasText(request.getReportType()), QuestionReport::getReportType, request.getReportType())
                .like(StringUtils.hasText(request.getKeyWord()), QuestionReport::getReportContent, request.getKeyWord())
                .orderByAsc(QuestionReport::getStatus).orderByDesc(QuestionReport::getId);
        Page<QuestionReport> page = new Page<>(request.getCurrent(), request.getPageSize());
        page(page, wrapper);
        return PageUtils.wrap(page, list -> list.stream().map(item -> {
            QuestionReportPageListResp response = QuestionReportConvert.INSTANCE.toQuestionReportPageListResp(item);
            QuestionBank question = questionBankService.getById(item.getBankQuestionId());
            response.setQuestionTitle(question == null ? "题目已删除" : question.getQuestionTitle());
            return response;
        }).collect(Collectors.toList()));
    }
}
