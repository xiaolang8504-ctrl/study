package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.review.entity.ReviewReminder;
import com.study.module.system.review.mapper.ReviewReminderMapper;
import com.study.module.system.review.service.ReviewReminderService;
import org.springframework.stereotype.Service;

/**
 * 复习提醒公共服务实现
 */
@Service
public class ReviewReminderServiceImpl extends ServiceImpl<ReviewReminderMapper, ReviewReminder>
        implements ReviewReminderService {
}
