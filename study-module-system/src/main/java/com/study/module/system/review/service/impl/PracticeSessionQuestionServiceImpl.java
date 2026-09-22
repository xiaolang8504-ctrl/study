package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.review.mapper.PracticeSessionQuestionMapper;
import com.study.module.system.review.service.PracticeSessionQuestionService;
import org.springframework.stereotype.Service;

/**
 * 专项练习题目公共服务实现
 */
@Service
public class PracticeSessionQuestionServiceImpl
        extends ServiceImpl<PracticeSessionQuestionMapper, PracticeSessionQuestion>
        implements PracticeSessionQuestionService {
}
