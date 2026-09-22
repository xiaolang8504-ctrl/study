package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.mapper.ReviewRecordMapper;
import com.study.module.system.review.service.ReviewRecordService;
import org.springframework.stereotype.Service;

/**
 * 复习记录公共服务实现
 */
@Service
public class ReviewRecordServiceImpl extends ServiceImpl<ReviewRecordMapper, ReviewRecord>
        implements ReviewRecordService {
}
