package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.wrongquestion.entity.QuestionCaptureTask;
import com.study.module.system.wrongquestion.mapper.QuestionCaptureTaskMapper;
import com.study.module.system.wrongquestion.service.QuestionCaptureTaskService;
import org.springframework.stereotype.Service;

/**
 * 题目采集任务实体服务实现
 */
@Service
public class QuestionCaptureTaskServiceImpl extends ServiceImpl<QuestionCaptureTaskMapper, QuestionCaptureTask>
        implements QuestionCaptureTaskService {
}
