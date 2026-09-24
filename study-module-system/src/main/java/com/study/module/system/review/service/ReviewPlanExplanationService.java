package com.study.module.system.review.service;

import com.study.module.system.review.dto.response.ReviewPlanExplanationResp;
import com.study.module.system.review.dto.response.ReviewTodayTaskResp;
import com.study.module.system.review.entity.ReviewPlan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 将复习算法结果转化为学生可理解的计划说明。 */
public interface ReviewPlanExplanationService {

    /** 查询个人历史用时后，得出单题预计分钟数。 */
    int estimatedMinutesPerQuestion(Long userId, String subject);

    /** 补充一道今日任务的到期原因、用时和不同反馈下的预估排期。 */
    void explainTask(ReviewTodayTaskResp task, int estimatedMinutes,
                     LocalDate reviewDate, LocalDateTime currentTime);

    /** 构建未来 7/30 日当前排期负荷。 */
    ReviewPlanExplanationResp buildPlanExplanation(ReviewPlan reviewPlan, String subject,
                                                    LocalDate reviewDate,
                                                    List<ReviewTodayTaskResp> taskList,
                                                    int estimatedMinutesPerQuestion);
}
