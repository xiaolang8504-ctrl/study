package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.questionbank.entity.QuestionReport;
import com.study.module.system.questionbank.mapper.QuestionReportMapper;
import com.study.module.system.questionbank.service.QuestionReportService;
import org.springframework.stereotype.Service;

/**
 * 题目举报公共服务实现
 */
@Service
public class QuestionReportServiceImpl extends ServiceImpl<QuestionReportMapper, QuestionReport>
        implements QuestionReportService {
}
