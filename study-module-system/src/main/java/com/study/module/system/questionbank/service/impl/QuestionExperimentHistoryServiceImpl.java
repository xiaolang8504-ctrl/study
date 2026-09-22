package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.questionbank.entity.QuestionExperimentHistory;
import com.study.module.system.questionbank.mapper.QuestionExperimentHistoryMapper;
import com.study.module.system.questionbank.service.QuestionExperimentHistoryService;
import org.springframework.stereotype.Service;

/**
 * 相似题实验历史服务实现。
 */
@Service
public class QuestionExperimentHistoryServiceImpl
        extends ServiceImpl<QuestionExperimentHistoryMapper, QuestionExperimentHistory>
        implements QuestionExperimentHistoryService {
}
