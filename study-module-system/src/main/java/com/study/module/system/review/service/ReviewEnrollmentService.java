package com.study.module.system.review.service;

import com.study.module.system.wrongquestion.entity.WrongQuestion;

/**
 * 错题加入智能复习计划服务
 */
public interface ReviewEnrollmentService {

    /**
     * 将已订正或已掌握错题幂等加入当前用户的复习计划。
     */
    void syncWrongQuestionReview(WrongQuestion wrongQuestion);

    /**
     * 错题删除时结束对应复习任务。
     */
    void finishWrongQuestions(java.util.List<Long> wrongQuestionIds, Long userId);
}
