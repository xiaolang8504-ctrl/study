package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.review.service.ReviewEnrollmentService;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.mapper.WrongQuestionMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.module.system.wrongquestion.service.WrongQuestionStatusService;
import com.study.module.system.wrongquestion.service.WrongQuestionTimelineService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 错题状态流转服务实现
 */
@Service
public class WrongQuestionStatusServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion>
        implements WrongQuestionStatusService {

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    ReviewEnrollmentService reviewEnrollmentService;

    @Autowired
    WrongQuestionTimelineService wrongQuestionTimelineService;

    /**
     * 更新错题状态
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWrongQuestionStatus(Long id, Integer status, String remark) {
        if (!WrongQuestionStatus.valid(status)) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_STATUS_INVALID);
        }
        WrongQuestion wrongQuestion = wrongQuestionService.checkWrongQuestion(id);
        Integer previousStatus = wrongQuestion.getStatus();
        checkStatusFlow(previousStatus, status);
        LocalDateTime now = LocalDateTime.now();
        boolean updated = this.lambdaUpdate()
                .eq(WrongQuestion::getId, id)
                .set(WrongQuestion::getStatus, status)
                .set(WrongQuestion::getUpdateTime, now)
                .update();
        if (!updated) {
            throw new LogicException(ErrorCodeConstants.UPDATE_WRONG_QUESTION_FAIL);
        }
        wrongQuestion.setStatus(status);
        wrongQuestion.setUpdateTime(now);
        wrongQuestionTimelineService.record(id, "STATUS_CHANGED", "STUDENT",
                "状态由" + statusName(previousStatus) + "变更为" + statusName(status)
                        + (remark == null || remark.trim().isEmpty() ? "" : "：" + remark), AccountUtils.getUserId());
        reviewEnrollmentService.syncWrongQuestionReview(wrongQuestion);
    }

    /**
     * 校验状态流转
     */
    private void checkStatusFlow(Integer currentStatus, Integer targetStatus) {
        if (targetStatus.equals(currentStatus)) {
            return;
        }
        // 订正必须留下订正记录，已掌握只能由独立作答的复习反馈推进。
        if (Integer.valueOf(WrongQuestionStatus.CORRECTED).equals(targetStatus)
                || Integer.valueOf(WrongQuestionStatus.MASTERED).equals(targetStatus)) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_STATUS_FLOW_INVALID);
        }
        // 允许学生将已订正题目重新打开订正；重新打开后会同步结束其复习任务。
        if (Integer.valueOf(WrongQuestionStatus.PENDING_CORRECTION).equals(targetStatus)
                && Integer.valueOf(WrongQuestionStatus.CORRECTED).equals(currentStatus)) {
            return;
        }
        // 归档是闭环的终态，仅允许已掌握题目进入归档。
        if (Integer.valueOf(WrongQuestionStatus.ARCHIVED).equals(targetStatus)
                && Integer.valueOf(WrongQuestionStatus.MASTERED).equals(currentStatus)) {
            return;
        }
        throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_STATUS_FLOW_INVALID);
    }

    private String statusName(Integer status) {
        if (Integer.valueOf(WrongQuestionStatus.PENDING_CORRECTION).equals(status)) return "待订正";
        if (Integer.valueOf(WrongQuestionStatus.CORRECTED).equals(status)) return "已订正";
        if (Integer.valueOf(WrongQuestionStatus.MASTERED).equals(status)) return "已掌握";
        if (Integer.valueOf(WrongQuestionStatus.ARCHIVED).equals(status)) return "已归档";
        return "未知状态";
    }
}
