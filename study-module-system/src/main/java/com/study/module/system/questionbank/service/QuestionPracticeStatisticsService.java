package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.response.QuestionPracticeStatisticsResp;
import com.study.module.system.questionbank.entity.QuestionRecommendationLog;

/**
 * 相似题练习统计服务
 */
public interface QuestionPracticeStatisticsService extends IService<QuestionRecommendationLog> {

    /**
     * 统计相似题练习数据
     */
    QuestionPracticeStatisticsResp questionPracticeStatistics();
}
