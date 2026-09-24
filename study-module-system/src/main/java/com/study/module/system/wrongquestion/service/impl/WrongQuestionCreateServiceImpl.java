package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.wrongquestion.convert.WrongQuestionConvert;
import com.study.module.system.wrongquestion.dto.request.CreateWrongQuestionReq;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.mapper.WrongQuestionMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionCreateService;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.module.system.wrongquestion.service.WrongQuestionTimelineService;
import com.study.module.system.wrongquestion.service.WrongQuestionAssetService;
import com.study.module.system.wrongquestion.utils.WrongQuestionContentUtils;
import com.study.module.system.wrongquestion.constants.WrongQuestionDiagnosisPolicy;
import com.study.module.system.review.service.ReviewEnrollmentService;
import com.study.module.system.questionbank.service.WrongQuestionKnowledgePointService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 初中生错题录入服务
 */
@Service
public class WrongQuestionCreateServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion> implements WrongQuestionCreateService {

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    ReviewEnrollmentService reviewEnrollmentService;

    @Autowired
    WrongQuestionKnowledgePointService wrongQuestionKnowledgePointService;

    @Autowired
    WrongQuestionTimelineService wrongQuestionTimelineService;

    @Autowired
    WrongQuestionAssetService wrongQuestionAssetService;

    /**
     * 错题录入
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createWrongQuestion(CreateWrongQuestionReq request) {
        WrongQuestion wrongQuestion = WrongQuestionConvert.INSTANCE.toWrongQuestion(request);
        wrongQuestionService.fillDictNames(wrongQuestion);
        wrongQuestion.setCreateId(AccountUtils.getUserId());
        wrongQuestion.setContentFormat(org.springframework.util.StringUtils.hasText(wrongQuestion.getContentFormat())
                ? wrongQuestion.getContentFormat() : "TEXT");
        wrongQuestion.setErrorCauseCodes(WrongQuestionDiagnosisPolicy.normalizeCauseCodes(
                request.getErrorCauseCodes(), request.getErrorLabels()));
        wrongQuestion.setErrorLabels(WrongQuestionDiagnosisPolicy.normalizeErrorLabels(
                request.getErrorLabels(), wrongQuestion.getErrorCauseCodes()));
        wrongQuestion.setQuestionFingerprint(WrongQuestionContentUtils.fingerprint(
                wrongQuestion.getQuestionTitle(), wrongQuestion.getQuestionContent(),
                wrongQuestion.getOptionsJson()));
        wrongQuestion.setCreateTime(LocalDateTime.now());
        wrongQuestion.setUpdateTime(LocalDateTime.now());
        if (!this.save(wrongQuestion)) {
            throw new LogicException(ErrorCodeConstants.CREATE_WRONG_QUESTION_FAIL);
        }
        wrongQuestionKnowledgePointService.rewrite(wrongQuestion.getId(), request.getKnowledgePointIds());
        wrongQuestionAssetService.rewriteAssets(wrongQuestion);
        wrongQuestionTimelineService.record(wrongQuestion.getId(), "CREATED", "STUDENT", "已手工录入错题", wrongQuestion.getCreateId());
        // 新增时已完成订正的错题立即进入复习计划，不等待访问复习首页。
        reviewEnrollmentService.syncWrongQuestionReview(wrongQuestion);
    }

}
