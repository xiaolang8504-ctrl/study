package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.questionbank.entity.QuestionRecommendationLog;
import com.study.module.system.questionbank.mapper.QuestionRecommendationLogMapper;
import com.study.module.system.questionbank.service.QuestionRecommendationLogService;
import org.springframework.stereotype.Service;

/**
 * 题目推荐记录公共服务实现
 */
@Service
public class QuestionRecommendationLogServiceImpl
        extends ServiceImpl<QuestionRecommendationLogMapper, QuestionRecommendationLog>
        implements QuestionRecommendationLogService {
}
