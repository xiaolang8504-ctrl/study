package com.study.module.system.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.review.dto.response.LearningMetricDailyResp;
import com.study.module.system.review.entity.LearningMetricDaily;

import java.time.LocalDate;
import java.util.List;

/**
 * 学习日快照服务。
 */
public interface LearningMetricDailyService extends IService<LearningMetricDaily> {

    /**
     * 为指定日期重建学习快照，可安全重复执行。
     */
    void buildDailySnapshot(LocalDate metricDate);

    /**
     * 查询单个学生指定科目的日趋势。
     */
    List<LearningMetricDailyResp> learningMetricTrend(Long userId, String subject,
                                                       LocalDate startDate, LocalDate endDate);
}
