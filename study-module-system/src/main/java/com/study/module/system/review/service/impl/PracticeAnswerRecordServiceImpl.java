package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.review.entity.PracticeAnswerRecord;
import com.study.module.system.review.mapper.PracticeAnswerRecordMapper;
import com.study.module.system.review.service.PracticeAnswerRecordService;
import org.springframework.stereotype.Service;

/**
 * 专项练习作答记录公共服务实现
 */
@Service
public class PracticeAnswerRecordServiceImpl
        extends ServiceImpl<PracticeAnswerRecordMapper, PracticeAnswerRecord>
        implements PracticeAnswerRecordService {
}
