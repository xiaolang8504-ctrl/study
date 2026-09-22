package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.module.system.review.dto.response.LearningDataBackupResp;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.entity.ReviewPlan;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.entity.ReviewSubjectSetting;
import com.study.module.system.review.service.LearningDataBackupService;
import com.study.module.system.review.service.LearningProfileService;
import com.study.module.system.review.service.PracticeSessionQuestionService;
import com.study.module.system.review.service.PracticeSessionService;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.review.service.ReviewPlanService;
import com.study.module.system.review.service.ReviewRecordService;
import com.study.module.system.review.service.ReviewSubjectSettingService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 个人学习数据备份服务实现。
 */
@Service
public class LearningDataBackupServiceImpl implements LearningDataBackupService {

    @Autowired
    private LearningProfileService learningProfileService;
    @Autowired
    private WrongQuestionService wrongQuestionService;
    @Autowired
    private ReviewPlanService reviewPlanService;
    @Autowired
    private ReviewSubjectSettingService reviewSubjectSettingService;
    @Autowired
    private ReviewItemService reviewItemService;
    @Autowired
    private ReviewRecordService reviewRecordService;
    @Autowired
    private PracticeSessionService practiceSessionService;
    @Autowired
    private PracticeSessionQuestionService practiceSessionQuestionService;

    @Override
    @Transactional(readOnly = true)
    public LearningDataBackupResp learningDataBackup() {
        Long userId = AccountUtils.getUserId();
        LearningDataBackupResp response = new LearningDataBackupResp();
        response.setBackupVersion("wrong-question-p1-v1");
        response.setGeneratedTime(LocalDateTime.now());
        response.setLearningProfile(learningProfileService.learningProfile());
        response.setReviewPlan(reviewPlanService.lambdaQuery()
                .eq(ReviewPlan::getUserId, userId).one());
        response.setReviewSubjectSettingList(reviewSubjectSettingService.lambdaQuery()
                .eq(ReviewSubjectSetting::getUserId, userId).orderByAsc(ReviewSubjectSetting::getId).list());
        response.setWrongQuestionList(wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getCreateId, userId).orderByAsc(WrongQuestion::getId).list());
        response.setReviewItemList(reviewItemService.lambdaQuery()
                .eq(ReviewItem::getUserId, userId).orderByAsc(ReviewItem::getId).list());
        response.setReviewRecordList(reviewRecordService.lambdaQuery()
                .eq(ReviewRecord::getUserId, userId).orderByAsc(ReviewRecord::getId).list());
        response.setPracticeSessionList(practiceSessionService.lambdaQuery()
                .eq(PracticeSession::getUserId, userId).orderByAsc(PracticeSession::getId).list());
        response.setPracticeSessionQuestionList(practiceSessionQuestionService.lambdaQuery()
                .eq(PracticeSessionQuestion::getUserId, userId)
                .orderByAsc(PracticeSessionQuestion::getId).list());
        return response;
    }
}
