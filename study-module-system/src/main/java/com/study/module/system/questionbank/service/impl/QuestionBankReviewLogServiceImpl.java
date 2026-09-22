package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.questionbank.entity.QuestionBankReviewLog;
import com.study.module.system.questionbank.mapper.QuestionBankReviewLogMapper;
import com.study.module.system.questionbank.service.QuestionBankReviewLogService;
import org.springframework.stereotype.Service;

/**
 * 题库审核记录服务实现。
 */
@Service
public class QuestionBankReviewLogServiceImpl
        extends ServiceImpl<QuestionBankReviewLogMapper, QuestionBankReviewLog>
        implements QuestionBankReviewLogService {
}
