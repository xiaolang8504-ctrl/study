package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.questionbank.dto.response.QuestionPracticeStatisticsResp;
import com.study.module.system.questionbank.entity.QuestionRecommendationLog;
import com.study.module.system.questionbank.mapper.QuestionRecommendationLogMapper;
import com.study.module.system.questionbank.service.QuestionPracticeStatisticsService;
import com.study.module.system.questionbank.service.QuestionReportService;
import com.study.module.system.questionbank.service.QuestionExperimentService;
import com.study.module.system.questionbank.entity.QuestionExperiment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 相似题练习统计服务实现
 */
@Service
public class QuestionPracticeStatisticsServiceImpl
        extends ServiceImpl<QuestionRecommendationLogMapper, QuestionRecommendationLog>
        implements QuestionPracticeStatisticsService {

    @Autowired
    QuestionReportService questionReportService;

    @Autowired
    QuestionExperimentService questionExperimentService;

    /**
     * 统计相似题练习数据
     */
    @Override
    public QuestionPracticeStatisticsResp questionPracticeStatistics() {
        QuestionExperiment experiment = questionExperimentService.currentRunningExperiment();
        LambdaQueryWrapper<QuestionRecommendationLog> window = window(experiment);
        long exposure = count(window);
        long answered = count(window(experiment).isNotNull(QuestionRecommendationLog::getAnswerTime));
        long correct = count(window(experiment).eq(QuestionRecommendationLog::getIsCorrect, 1));
        long groupAAnswered = groupCount("A", false, experiment);
        long groupACorrect = groupCount("A", true, experiment);
        long groupBAnswered = groupCount("B", false, experiment);
        long groupBCorrect = groupCount("B", true, experiment);
        QuestionPracticeStatisticsResp response = new QuestionPracticeStatisticsResp();
        response.setExposureCount(exposure);
        response.setAnsweredCount(answered);
        response.setCorrectCount(correct);
        response.setReportCount(questionReportService.count());
        response.setAnswerRate(rate(answered, exposure));
        response.setCorrectRate(rate(correct, answered));
        response.setGroupAAnsweredCount(groupAAnswered);
        response.setGroupACorrectRate(rate(groupACorrect, groupAAnswered));
        response.setGroupBAnsweredCount(groupBAnswered);
        response.setGroupBCorrectRate(rate(groupBCorrect, groupBAnswered));
        response.setExperiment(questionExperimentService.questionExperimentDetail());
        response.setLift(Math.round((response.getGroupBCorrectRate() - response.getGroupACorrectRate()) * 100D) / 100D);
        double z = zScore(groupACorrect, groupAAnswered, groupBCorrect, groupBAnswered);
        double p = twoSidedP(z);
        response.setZScore(Math.round(z * 1000D) / 1000D);
        response.setPValue(Math.round(p * 10000D) / 10000D);
        response.setSignificant(groupAAnswered > 0 && groupBAnswered > 0 && p < 0.05D);
        return response;
    }

    /**
     * 统计指定实验分组的作答或正确数量
     */
    private long groupCount(String group, boolean correct, QuestionExperiment experiment) {
        LambdaQueryWrapper<QuestionRecommendationLog> wrapper = window(experiment)
                .eq(QuestionRecommendationLog::getExperimentGroup, group);
        if (correct) {
            wrapper.eq(QuestionRecommendationLog::getIsCorrect, 1);
        } else {
            wrapper.isNotNull(QuestionRecommendationLog::getAnswerTime);
        }
        return count(wrapper);
    }

    /**
     * 执行 window 辅助处理。
     */
    private LambdaQueryWrapper<QuestionRecommendationLog> window(QuestionExperiment experiment) {
        LambdaQueryWrapper<QuestionRecommendationLog> wrapper = new LambdaQueryWrapper<>();
        if (experiment != null) {
            wrapper.eq(QuestionRecommendationLog::getExperimentId, experiment.getId())
                    .between(QuestionRecommendationLog::getExposureTime, experiment.getStartTime(), experiment.getEndTime());
        }
        return wrapper;
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

    /**
     * 计算百分比
     */
    private double rate(long value, long total) {
        return total == 0 ? 0D : Math.round(value * 10000D / total) / 100D;
    }
}
