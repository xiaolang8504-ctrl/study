package com.study.module.system.review.service;

/**
 * 专项练习进度同步服务
 */
public interface PracticeProgressSyncService {

    /**
     * 同步专项练习作答结果到智能复习进度
     *
     * @param wrongQuestionId 错题ID
     * @param userId 用户ID
     * @param correct 是否答对
     */
    void syncPracticeAnswer(Long wrongQuestionId, Long userId, boolean correct);
}
