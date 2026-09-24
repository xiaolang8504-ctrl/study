package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.wrongquestion.convert.WrongQuestionConvert;
import com.study.module.system.wrongquestion.dto.request.UpdateWrongQuestionImageReq;
import com.study.module.system.wrongquestion.dto.request.UpdateWrongQuestionReq;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.mapper.WrongQuestionMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.module.system.wrongquestion.service.WrongQuestionUpdateService;
import com.study.module.system.wrongquestion.service.WrongQuestionAssetService;
import com.study.module.system.wrongquestion.utils.WrongQuestionContentUtils;
import com.study.module.system.wrongquestion.constants.WrongQuestionDiagnosisPolicy;
import com.study.module.system.review.service.ReviewEnrollmentService;
import com.study.module.system.questionbank.service.WrongQuestionKnowledgePointService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 初中生错题修改服务
 */
@Service
public class WrongQuestionUpdateServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion> implements WrongQuestionUpdateService {

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    ReviewEnrollmentService reviewEnrollmentService;

    @Autowired
    WrongQuestionKnowledgePointService wrongQuestionKnowledgePointService;

    @Autowired
    WrongQuestionAssetService wrongQuestionAssetService;

    /**
     * 错题修改
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWrongQuestion(UpdateWrongQuestionReq request) {
        WrongQuestion existingWrongQuestion = wrongQuestionService.checkWrongQuestion(request.getId());
        WrongQuestion wrongQuestion = WrongQuestionConvert.INSTANCE.toWrongQuestion(request);
        wrongQuestion.setContentFormat(org.springframework.util.StringUtils.hasText(wrongQuestion.getContentFormat())
                ? wrongQuestion.getContentFormat() : "TEXT");
        wrongQuestion.setErrorCauseCodes(WrongQuestionDiagnosisPolicy.normalizeCauseCodes(
                request.getErrorCauseCodes(), request.getErrorLabels()));
        wrongQuestion.setErrorLabels(WrongQuestionDiagnosisPolicy.normalizeErrorLabels(
                request.getErrorLabels(), wrongQuestion.getErrorCauseCodes()));
        wrongQuestion.setQuestionFingerprint(WrongQuestionContentUtils.fingerprint(
                wrongQuestion.getQuestionTitle(), wrongQuestion.getQuestionContent(),
                wrongQuestion.getOptionsJson()));
        wrongQuestionService.fillDictNames(wrongQuestion);
        wrongQuestion.setUpdateTime(LocalDateTime.now());
        if (!this.updateById(wrongQuestion)) {
            throw new LogicException(ErrorCodeConstants.UPDATE_WRONG_QUESTION_FAIL);
        }
        wrongQuestionKnowledgePointService.rewrite(wrongQuestion.getId(), request.getKnowledgePointIds());
        // 使用原记录的所属用户，确保状态变为已订正后在同一事务内创建复习任务。
        wrongQuestion.setCreateId(existingWrongQuestion.getCreateId());
        wrongQuestion.setCapturePageId(existingWrongQuestion.getCapturePageId());
        wrongQuestion.setCaptureLeftPosition(existingWrongQuestion.getCaptureLeftPosition());
        wrongQuestion.setCaptureTopPosition(existingWrongQuestion.getCaptureTopPosition());
        wrongQuestion.setCaptureWidth(existingWrongQuestion.getCaptureWidth());
        wrongQuestion.setCaptureHeight(existingWrongQuestion.getCaptureHeight());
        wrongQuestionAssetService.rewriteAssets(wrongQuestion);
        reviewEnrollmentService.syncWrongQuestionReview(wrongQuestion);
    }

    /**
     * 错图修改
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWrongQuestionImage(UpdateWrongQuestionImageReq request) {
        WrongQuestion wrongQuestion = wrongQuestionService.checkWrongQuestion(request.getId());
        boolean updated = this.lambdaUpdate()
                .eq(WrongQuestion::getId, request.getId())
                .set(WrongQuestion::getImageUrl, request.getImageUrl())
                .set(WrongQuestion::getImageUrl2, request.getImageUrl2())
                .set(WrongQuestion::getImageUrl3, request.getImageUrl3())
                .set(WrongQuestion::getImageUrl4, request.getImageUrl4())
                .set(WrongQuestion::getUpdateTime, LocalDateTime.now())
                .update();
        if (!updated) {
            throw new LogicException(ErrorCodeConstants.UPDATE_WRONG_QUESTION_IMAGE_FAIL);
        }
        wrongQuestion.setImageUrl(request.getImageUrl());
        wrongQuestion.setImageUrl2(request.getImageUrl2());
        wrongQuestion.setImageUrl3(request.getImageUrl3());
        wrongQuestion.setImageUrl4(request.getImageUrl4());
        wrongQuestionAssetService.rewriteAssets(wrongQuestion);
    }
}
