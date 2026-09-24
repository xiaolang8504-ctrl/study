package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.review.entity.ReviewExamSprint;
import com.study.module.system.review.mapper.ReviewExamSprintMapper;
import com.study.module.system.review.service.ReviewExamSprintService;
import org.springframework.stereotype.Service;

@Service
public class ReviewExamSprintServiceImpl extends ServiceImpl<ReviewExamSprintMapper, ReviewExamSprint>
        implements ReviewExamSprintService {
}
