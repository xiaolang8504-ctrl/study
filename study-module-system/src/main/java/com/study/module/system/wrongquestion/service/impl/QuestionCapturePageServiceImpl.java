package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.wrongquestion.entity.QuestionCapturePage;
import com.study.module.system.wrongquestion.mapper.QuestionCapturePageMapper;
import com.study.module.system.wrongquestion.service.QuestionCapturePageService;
import org.springframework.stereotype.Service;

/**
 * 题目采集页面实体服务实现
 */
@Service
public class QuestionCapturePageServiceImpl extends ServiceImpl<QuestionCapturePageMapper, QuestionCapturePage>
        implements QuestionCapturePageService {
}
