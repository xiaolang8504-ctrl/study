package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.review.entity.ReviewPlan;
import com.study.module.system.review.mapper.ReviewPlanMapper;
import com.study.module.system.review.service.ReviewPlanService;
import org.springframework.stereotype.Service;

/**
 * 复习计划公共服务实现
 */
@Service
public class ReviewPlanServiceImpl extends ServiceImpl<ReviewPlanMapper, ReviewPlan>
        implements ReviewPlanService {
}
