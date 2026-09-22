package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.questionbank.entity.QuestionBankVersion;
import com.study.module.system.questionbank.mapper.QuestionBankVersionMapper;
import com.study.module.system.questionbank.service.QuestionBankVersionService;
import org.springframework.stereotype.Service;

/**
 * 题库版本服务实现。
 */
@Service
public class QuestionBankVersionServiceImpl extends ServiceImpl<QuestionBankVersionMapper, QuestionBankVersion>
        implements QuestionBankVersionService {
}
