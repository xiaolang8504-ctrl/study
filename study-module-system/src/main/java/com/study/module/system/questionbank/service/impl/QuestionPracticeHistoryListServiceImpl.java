package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.questionbank.convert.QuestionBankConvert;
import com.study.module.system.questionbank.dto.request.QuestionPracticeHistoryPageListReq;
import com.study.module.system.questionbank.dto.response.QuestionPracticeHistoryPageListResp;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.entity.QuestionRecommendationLog;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.study.module.system.questionbank.service.QuestionPracticeHistoryListService;
import com.study.module.system.questionbank.service.QuestionRecommendationLogService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 相似题练习历史列表服务实现。
 */
@Service
public class QuestionPracticeHistoryListServiceImpl implements QuestionPracticeHistoryListService {

    @Autowired
    private QuestionRecommendationLogService questionRecommendationLogService;

    @Autowired
    private QuestionBankService questionBankService;

    @Autowired
    private WrongQuestionService wrongQuestionService;

    /**
     * 分页查询当前用户的相似题练习历史。
     */
    @Override
    public PageResult<QuestionPracticeHistoryPageListResp> questionPracticeHistoryPageList(QuestionPracticeHistoryPageListReq request) {
        Page<QuestionRecommendationLog> page = new Page<>(request.getCurrent(), request.getPageSize());
        questionRecommendationLogService.page(page, new LambdaQueryWrapper<QuestionRecommendationLog>()
                .eq(QuestionRecommendationLog::getUserId, AccountUtils.getUserId())
                .eq(request.getIsCorrect() != null, QuestionRecommendationLog::getIsCorrect, request.getIsCorrect())
                .eq(StringUtils.hasText(request.getExperimentGroup()), QuestionRecommendationLog::getExperimentGroup, request.getExperimentGroup())
                .orderByDesc(QuestionRecommendationLog::getCreateTime, QuestionRecommendationLog::getId));
        List<Long> bankIds = page.getRecords().stream().map(QuestionRecommendationLog::getBankQuestionId)
                .distinct().collect(Collectors.toList());
        List<Long> wrongIds = page.getRecords().stream().map(QuestionRecommendationLog::getWrongQuestionId)
                .distinct().collect(Collectors.toList());
        Map<Long, QuestionBank> bankMap = bankIds.isEmpty() ? Collections.emptyMap()
                : questionBankService.listByIds(bankIds).stream()
                .collect(Collectors.toMap(QuestionBank::getId, Function.identity()));
        Map<Long, WrongQuestion> wrongMap = wrongIds.isEmpty() ? Collections.emptyMap()
                : wrongQuestionService.listByIds(wrongIds).stream()
                .collect(Collectors.toMap(WrongQuestion::getId, Function.identity()));
        return PageUtils.wrap(page, rows -> rows.stream().map(row -> {
            QuestionPracticeHistoryPageListResp response = QuestionBankConvert.INSTANCE.toQuestionPracticeHistoryPageListResp(row);
            QuestionBank bank = bankMap.get(row.getBankQuestionId());
            WrongQuestion wrong = wrongMap.get(row.getWrongQuestionId());
            response.setQuestionTitle(bank == null ? "题目已删除" : bank.getQuestionTitle());
            response.setWrongQuestionTitle(wrong == null ? "错题已删除" : wrong.getQuestionTitle());
            return response;
        }).collect(Collectors.toList()));
    }
}
