package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.mapper.WrongQuestionMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionDeleteService;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.review.service.ReviewEnrollmentService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 初中生错题删除服务
 */
@Service
public class WrongQuestionDeleteServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion> implements WrongQuestionDeleteService {

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    ReviewEnrollmentService reviewEnrollmentService;

    /**
     * 错题删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWrongQuestion(Long id) {
        wrongQuestionService.checkWrongQuestion(id);
        Long userId = AccountUtils.getUserId();
        reviewEnrollmentService.finishWrongQuestions(java.util.Collections.singletonList(id), userId);
        if (!this.lambdaUpdate().eq(WrongQuestion::getId, id)
                .eq(WrongQuestion::getCreateId, userId).remove()) {
            throw new LogicException(ErrorCodeConstants.DELETE_WRONG_QUESTION_FAIL);
        }
    }

    /**
     * 错题批量删除
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteWrongQuestion(List<Long> ids) {
        Long userId = AccountUtils.getUserId();
        long ownedCount = this.lambdaQuery()
                .eq(WrongQuestion::getCreateId, userId)
                .in(WrongQuestion::getId, ids)
                .count();
        long distinctCount = ids.stream().distinct().count();
        if (ownedCount != distinctCount) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_NOT_EXIST);
        }
        reviewEnrollmentService.finishWrongQuestions(ids, userId);
        if (!this.lambdaUpdate().eq(WrongQuestion::getCreateId, userId)
                .in(WrongQuestion::getId, ids).remove()) {
            throw new LogicException(ErrorCodeConstants.DELETE_WRONG_QUESTION_FAIL);
        }
    }
}
