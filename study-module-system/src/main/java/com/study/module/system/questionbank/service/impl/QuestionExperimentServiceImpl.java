package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.dto.request.QuestionExperimentSaveReq;
import com.study.module.system.questionbank.dto.response.QuestionExperimentHistoryResp;
import com.study.module.system.questionbank.dto.response.QuestionExperimentResp;
import com.study.module.system.questionbank.convert.QuestionBankConvert;
import com.study.module.system.questionbank.entity.QuestionExperiment;
import com.study.module.system.questionbank.entity.QuestionExperimentHistory;
import com.study.module.system.questionbank.mapper.QuestionExperimentMapper;
import com.study.module.system.questionbank.entity.QuestionRecommendationLog;
import com.study.module.system.questionbank.service.QuestionExperimentHistoryService;
import com.study.module.system.questionbank.service.QuestionExperimentService;
import com.study.module.system.questionbank.service.QuestionRecommendationLogService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 相似题 A/B 实验服务实现。
 */
@Service
public class QuestionExperimentServiceImpl extends ServiceImpl<QuestionExperimentMapper, QuestionExperiment>
        implements QuestionExperimentService {

    @Autowired
    QuestionExperimentHistoryService questionExperimentHistoryService;

    @Autowired
    QuestionRecommendationLogService questionRecommendationLogService;

    /**
     * 查询相似题 A/B 实验详情。
     */
    @Override
    public QuestionExperimentResp questionExperimentDetail() {
        QuestionExperiment entity = getOne(new LambdaQueryWrapper<QuestionExperiment>()
                .orderByDesc(QuestionExperiment::getId).last("LIMIT 1"));
        return entity == null ? null : toResponse(entity);
    }

    /**
     * 创建或保存相似题 A/B 实验。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestionExperiment(QuestionExperimentSaveReq request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new LogicException(ErrorCodeConstants.QUESTION_EXPERIMENT_TIME_INVALID);
        }
        LocalDateTime now = LocalDateTime.now();
        QuestionExperiment entity = request.getId() == null ? new QuestionExperiment() : getById(request.getId());
        if (entity == null) {
            throw new LogicException(ErrorCodeConstants.QUESTION_EXPERIMENT_NOT_EXIST);
        }
        QuestionBankConvert.INSTANCE.updateQuestionExperiment(request, entity);
        entity.setUpdateTime(now);
        if (entity.getId() == null) {
            entity.setCreateId(AccountUtils.getUserId());
            entity.setCreateTime(now);
        }
        if (entity.getEnable() == 1) {
            lambdaUpdate().ne(entity.getId() != null, QuestionExperiment::getId, entity.getId())
                    .set(QuestionExperiment::getEnable, 0)
                    .set(QuestionExperiment::getUpdateTime, now)
                    .update();
        }
        saveOrUpdate(entity);
        QuestionExperimentHistory history = QuestionBankConvert.INSTANCE.toQuestionExperimentHistory(entity);
        history.setExperimentId(entity.getId());
        history.setOperatorId(AccountUtils.getUserId());
        history.setCreateTime(now);
        questionExperimentHistoryService.save(history);
    }

    /**
     * 查询相似题 A/B 实验历史记录。
     */
    @Override
    public List<QuestionExperimentHistoryResp> questionExperimentHistory() {
        List<QuestionExperimentHistory> rows = questionExperimentHistoryService.list(
                new LambdaQueryWrapper<QuestionExperimentHistory>().orderByDesc(QuestionExperimentHistory::getId).last("LIMIT 100"));
        return rows == null ? Collections.emptyList() : rows.stream().map(item -> {
            QuestionExperimentHistoryResp response = QuestionBankConvert.INSTANCE.toQuestionExperimentHistoryResp(item);
            response.setHistoryId(item.getId());
            response.setId(item.getExperimentId());
            response.setGroupBTraffic(100 - item.getGroupATraffic());
            response.setOperationTime(item.getCreateTime());
            fillHistoryStatistics(response, item.getExperimentId());
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 获取相关业务数据。
     */
    @Override
    public QuestionExperiment currentRunningExperiment() {
        LocalDateTime now = LocalDateTime.now();
        return getOne(new LambdaQueryWrapper<QuestionExperiment>().eq(QuestionExperiment::getEnable, 1)
                .le(QuestionExperiment::getStartTime, now).ge(QuestionExperiment::getEndTime, now)
                .orderByDesc(QuestionExperiment::getId).last("LIMIT 1"));
    }

    /**
     * 获取相关业务数据。
     */
    @Override
    public String assignGroup(Long userId) {
        QuestionExperiment experiment = currentRunningExperiment();
        int traffic = experiment == null ? 100 : experiment.getGroupATraffic();
        int bucket = Math.floorMod(Long.hashCode(userId), 100);
        return bucket < traffic ? "A" : "B";
    }

    /**
     * 转换业务数据。
     */
    private QuestionExperimentResp toResponse(QuestionExperiment entity) {
        QuestionExperimentResp response = QuestionBankConvert.INSTANCE.toQuestionExperimentResp(entity);
        response.setGroupBTraffic(100 - entity.getGroupATraffic());
        LocalDateTime now = LocalDateTime.now();
        response.setRunning(entity.getEnable() == 1 && !now.isBefore(entity.getStartTime()) && !now.isAfter(entity.getEndTime()));
        return response;
    }

    /**
     * 更新业务数据。
     */
    private void fillHistoryStatistics(QuestionExperimentHistoryResp response, Long experimentId) {
        long aAnswered = logCount(experimentId, "A", false);
        long aCorrect = logCount(experimentId, "A", true);
        long bAnswered = logCount(experimentId, "B", false);
        long bCorrect = logCount(experimentId, "B", true);
        double aRate = rate(aCorrect, aAnswered);
        double bRate = rate(bCorrect, bAnswered);
        double z = zScore(aCorrect, aAnswered, bCorrect, bAnswered);
        double p = twoSidedP(z);
        response.setGroupAAnsweredCount(aAnswered);
        response.setGroupACorrectRate(aRate);
        response.setGroupBAnsweredCount(bAnswered);
        response.setGroupBCorrectRate(bRate);
        response.setLift(Math.round((bRate - aRate) * 100D) / 100D);
        response.setPValue(Math.round(p * 10000D) / 10000D);
        response.setSignificant(aAnswered > 0 && bAnswered > 0 && p < 0.05D);
    }

    /**
     * 执行 logCount 辅助处理。
     */
    private long logCount(Long experimentId, String group, boolean correct) {
        LambdaQueryWrapper<QuestionRecommendationLog> wrapper = new LambdaQueryWrapper<QuestionRecommendationLog>()
                .eq(QuestionRecommendationLog::getExperimentId, experimentId)
                .eq(QuestionRecommendationLog::getExperimentGroup, group);
        if (correct) wrapper.eq(QuestionRecommendationLog::getIsCorrect, 1);
        else wrapper.isNotNull(QuestionRecommendationLog::getAnswerTime);
        return questionRecommendationLogService.count(wrapper);
    }

    /**
     * 标准化并计算业务数据。
     */
    private double rate(long value, long total) {
        return total == 0 ? 0D : Math.round(value * 10000D / total) / 100D;
    }

    /**
     * 执行 zScore 辅助处理。
     */
    private double zScore(long correctA, long totalA, long correctB, long totalB) {
        if (totalA == 0 || totalB == 0) return 0D;
        double pooled = (correctA + correctB) * 1D / (totalA + totalB);
        double standardError = Math.sqrt(pooled * (1D - pooled) * (1D / totalA + 1D / totalB));
        return standardError == 0D ? 0D : (correctB * 1D / totalB - correctA * 1D / totalA) / standardError;
    }

    /**
     * 执行 twoSidedP 辅助处理。
     */
    private double twoSidedP(double z) {
        double value = Math.abs(z);
        double t = 1D / (1D + 0.2316419D * value);
        double density = 0.3989422804D * Math.exp(-value * value / 2D);
        double tail = density * t * (0.319381530D + t * (-0.356563782D + t * (1.781477937D
                + t * (-1.821255978D + t * 1.330274429D))));
        return Math.min(1D, 2D * tail);
    }
}
