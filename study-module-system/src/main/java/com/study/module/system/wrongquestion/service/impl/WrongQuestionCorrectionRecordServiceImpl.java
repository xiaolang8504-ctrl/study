package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.review.service.ReviewEnrollmentService;
import com.study.module.system.user.service.UserService;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.convert.WrongQuestionConvert;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionCorrectionRecordReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionCorrectionRecordResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.entity.WrongQuestionCorrectionRecord;
import com.study.module.system.wrongquestion.mapper.WrongQuestionCorrectionRecordMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionCorrectionRecordService;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.module.system.wrongquestion.service.WrongQuestionTimelineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 错题订正记录服务实现
 */
@Service
public class WrongQuestionCorrectionRecordServiceImpl
        extends ServiceImpl<WrongQuestionCorrectionRecordMapper, WrongQuestionCorrectionRecord>
        implements WrongQuestionCorrectionRecordService {

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    UserService userService;

    @Autowired
    ReviewEnrollmentService reviewEnrollmentService;

    @Autowired
    WrongQuestionTimelineService wrongQuestionTimelineService;

    /**
     * 提交错题订正记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitCorrectionRecord(WrongQuestionCorrectionRecordReq request) {
        WrongQuestion wrongQuestion = wrongQuestionService.checkWrongQuestion(request.getWrongQuestionId());
        if (Integer.valueOf(WrongQuestionStatus.MASTERED).equals(wrongQuestion.getStatus())
                || Integer.valueOf(WrongQuestionStatus.ARCHIVED).equals(wrongQuestion.getStatus())) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_STATUS_FLOW_INVALID);
        }
        LocalDateTime now = LocalDateTime.now();
        WrongQuestionCorrectionRecord record = new WrongQuestionCorrectionRecord();
        record.setWrongQuestionId(request.getWrongQuestionId());
        record.setCorrectionAnswer(request.getCorrectionAnswer());
        record.setCorrectionAnalysis(request.getCorrectionAnalysis());
        record.setCorrectionImageUrl(request.getCorrectionImageUrl());
        record.setCorrectionRemark(request.getCorrectionRemark());
        record.setBeforeStatus(wrongQuestion.getStatus());
        record.setAfterStatus(WrongQuestionStatus.CORRECTED);
        record.setCreateId(userService.getUserIdByToken());
        record.setCreateTime(now);
        record.setUpdateTime(now);
        if (!this.save(record)) {
            throw new LogicException(ErrorCodeConstants.CREATE_WRONG_QUESTION_CORRECTION_RECORD_FAIL);
        }
        boolean updated = wrongQuestionService.lambdaUpdate()
                .eq(WrongQuestion::getId, wrongQuestion.getId())
                .set(WrongQuestion::getStatus, WrongQuestionStatus.CORRECTED)
                .set(WrongQuestion::getUpdateTime, now)
                .update();
        if (!updated) {
            throw new LogicException(ErrorCodeConstants.UPDATE_WRONG_QUESTION_FAIL);
        }
        wrongQuestion.setStatus(WrongQuestionStatus.CORRECTED);
        wrongQuestion.setUpdateTime(now);
        wrongQuestionTimelineService.record(wrongQuestion.getId(), "CORRECTION_SUBMITTED", "STUDENT",
                "已提交订正，状态变更为已订正并加入复习计划", record.getCreateId());
        reviewEnrollmentService.syncWrongQuestionReview(wrongQuestion);
    }

    /**
     * 错题订正记录列表
     */
    @Override
    public List<WrongQuestionCorrectionRecordResp> correctionRecordList(Long wrongQuestionId) {
        wrongQuestionService.checkWrongQuestion(wrongQuestionId);
        return this.lambdaQuery()
                .eq(WrongQuestionCorrectionRecord::getWrongQuestionId, wrongQuestionId)
                .orderByDesc(WrongQuestionCorrectionRecord::getId)
                .list()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 最新错题订正记录
     */
    @Override
    public WrongQuestionCorrectionRecordResp latestCorrectionRecord(Long wrongQuestionId) {
        WrongQuestionCorrectionRecord record = this.lambdaQuery()
                .eq(WrongQuestionCorrectionRecord::getWrongQuestionId, wrongQuestionId)
                .orderByDesc(WrongQuestionCorrectionRecord::getId)
                .last("limit 1")
                .one();
        return record == null ? null : toResponse(record);
    }

    /**
     * 转换响应
     */
    private WrongQuestionCorrectionRecordResp toResponse(WrongQuestionCorrectionRecord record) {
        return WrongQuestionConvert.INSTANCE.toWrongQuestionCorrectionRecordResp(record);
    }
}
