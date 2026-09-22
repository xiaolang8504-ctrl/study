package com.study.module.system.review.service.impl;

import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.review.constants.PracticeSessionStatus;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.service.PracticeSessionDetailService;
import com.study.module.system.review.service.PracticeSessionFinishService;
import com.study.module.system.review.service.PracticeSessionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 完成专项练习服务实现
 */
@Service
public class PracticeSessionFinishServiceImpl implements PracticeSessionFinishService {

    @Autowired
    PracticeSessionService practiceSessionService;

    @Autowired
    PracticeSessionDetailService practiceSessionDetailService;

    /**
     * 执行 finishPracticeSession 业务处理。
     */
    @Override
    public PracticeSessionDetailResp finishPracticeSession(Long sessionId) {
        Long userId = AccountUtils.getUserId();
        PracticeSession session = practiceSessionService.lambdaQuery()
                .eq(PracticeSession::getId, sessionId)
                .eq(PracticeSession::getUserId, userId)
                .one();
        if (session == null) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_NOT_EXIST);
        }
        session.setStatus(PracticeSessionStatus.FINISHED);
        session.setFinishTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        practiceSessionService.updateById(session);
        return practiceSessionDetailService.practiceSessionDetail(sessionId);
    }
}
