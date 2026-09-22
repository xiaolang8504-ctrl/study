package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.mapper.PracticeSessionMapper;
import com.study.module.system.review.service.PracticeSessionService;
import org.springframework.stereotype.Service;

/**
 * 专项练习会话公共服务实现
 */
@Service
public class PracticeSessionServiceImpl extends ServiceImpl<PracticeSessionMapper, PracticeSession>
        implements PracticeSessionService {
}
