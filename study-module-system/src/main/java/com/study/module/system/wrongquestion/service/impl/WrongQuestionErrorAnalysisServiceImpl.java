package com.study.module.system.wrongquestion.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionErrorAnalysisReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionErrorAnalysisStatisticsReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionErrorAnalysisStatisticsResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.mapper.WrongQuestionMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionErrorAnalysisService;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 错题错因分析服务实现
 */
@Service
public class WrongQuestionErrorAnalysisServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion>
        implements WrongQuestionErrorAnalysisService {

    private static final String UNMARKED_ERROR_LABEL = "未标记";

    @Autowired
    private WrongQuestionService wrongQuestionService;

    /**
     * 更新错题错因分析
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWrongQuestionErrorAnalysis(WrongQuestionErrorAnalysisReq request) {
        wrongQuestionService.checkWrongQuestion(request.getId());
        boolean updated = this.lambdaUpdate()
                .eq(WrongQuestion::getId, request.getId())
                .set(WrongQuestion::getErrorLabels, normalizeErrorLabels(request.getErrorLabels()))
                .set(WrongQuestion::getWrongReason, request.getWrongReason())
                .set(WrongQuestion::getUpdateTime, LocalDateTime.now())
                .update();
        if (!updated) {
            throw new LogicException(ErrorCodeConstants.UPDATE_WRONG_QUESTION_FAIL);
        }
    }

    /**
     * 错因分析统计
     */
    @Override
    public List<WrongQuestionErrorAnalysisStatisticsResp> wrongQuestionErrorAnalysisStatistics(
            WrongQuestionErrorAnalysisStatisticsReq request) {
        List<WrongQuestion> wrongQuestions = this.list(buildWrongQuestionStatisticsQuery(request));
        Map<String, WrongQuestionErrorAnalysisStatisticsResp> statisticsMap = new java.util.HashMap<>();
        for (WrongQuestion wrongQuestion : wrongQuestions) {
            for (String errorLabel : parseErrorLabels(wrongQuestion.getErrorLabels())) {
                WrongQuestionErrorAnalysisStatisticsResp statistics = statisticsMap.computeIfAbsent(
                        errorLabel, this::buildStatistics);
                increaseStatusCount(statistics, wrongQuestion.getStatus());
            }
        }
        return statisticsMap.values().stream()
                .sorted(Comparator.comparing(WrongQuestionErrorAnalysisStatisticsResp::getWrongQuestionCount)
                        .reversed())
                .collect(Collectors.toList());
    }

    /**
     * 构建错题查询
     */
    private LambdaQueryWrapper<WrongQuestion> buildWrongQuestionStatisticsQuery(
            WrongQuestionErrorAnalysisStatisticsReq request) {
        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(WrongQuestion::getErrorLabels, WrongQuestion::getStatus)
                .eq(WrongQuestion::getCreateId, AccountUtils.getUserId());
        if (StringUtils.hasText(request.getGrade())) {
            queryWrapper.eq(WrongQuestion::getGrade, request.getGrade());
        }
        if (StringUtils.hasText(request.getSubject())) {
            queryWrapper.eq(WrongQuestion::getSubject, request.getSubject());
        }
        if (request.getStatus() != null) {
            queryWrapper.eq(WrongQuestion::getStatus, request.getStatus());
        }
        return queryWrapper;
    }

    /**
     * 规范化错误标签
     */
    private String normalizeErrorLabels(String errorLabels) {
        List<String> labels = parseErrorLabels(errorLabels);
        labels.remove(UNMARKED_ERROR_LABEL);
        return String.join(",", labels);
    }

    /**
     * 解析错误标签
     */
    private List<String> parseErrorLabels(String errorLabels) {
        if (StrUtil.isBlank(errorLabels)) {
            List<String> labels = new ArrayList<>();
            labels.add(UNMARKED_ERROR_LABEL);
            return labels;
        }
        return java.util.Arrays.stream(errorLabels.split("[,，]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ArrayList::new));
    }

    /**
     * 构建统计响应
     */
    private WrongQuestionErrorAnalysisStatisticsResp buildStatistics(String errorLabel) {
        WrongQuestionErrorAnalysisStatisticsResp response = new WrongQuestionErrorAnalysisStatisticsResp();
        response.setErrorLabel(errorLabel);
        response.setWrongQuestionCount(0);
        response.setPendingCorrectionCount(0);
        response.setCorrectedCount(0);
        response.setMasteredCount(0);
        response.setArchivedCount(0);
        return response;
    }

    /**
     * 累加状态数量
     */
    private void increaseStatusCount(WrongQuestionErrorAnalysisStatisticsResp statistics, Integer status) {
        statistics.setWrongQuestionCount(statistics.getWrongQuestionCount() + 1);
        if (Integer.valueOf(WrongQuestionStatus.PENDING_CORRECTION).equals(status)) {
            statistics.setPendingCorrectionCount(statistics.getPendingCorrectionCount() + 1);
        } else if (Integer.valueOf(WrongQuestionStatus.CORRECTED).equals(status)) {
            statistics.setCorrectedCount(statistics.getCorrectedCount() + 1);
        } else if (Integer.valueOf(WrongQuestionStatus.MASTERED).equals(status)) {
            statistics.setMasteredCount(statistics.getMasteredCount() + 1);
        } else if (Integer.valueOf(WrongQuestionStatus.ARCHIVED).equals(status)) {
            statistics.setArchivedCount(statistics.getArchivedCount() + 1);
        }
    }
}
