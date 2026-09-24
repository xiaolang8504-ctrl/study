package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.review.entity.PracticePaperAnswerRecord;
import com.study.module.system.review.mapper.PracticePaperAnswerRecordMapper;
import com.study.module.system.review.service.PracticePaperAnswerRecordService;
import org.springframework.stereotype.Service;

/**
 * 纸面练习卷逐题回填记录公共服务实现。
 */
@Service
public class PracticePaperAnswerRecordServiceImpl
        extends ServiceImpl<PracticePaperAnswerRecordMapper, PracticePaperAnswerRecord>
        implements PracticePaperAnswerRecordService {
}
