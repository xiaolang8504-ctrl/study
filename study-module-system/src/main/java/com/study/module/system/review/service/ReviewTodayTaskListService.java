package com.study.module.system.review.service;

import com.study.module.system.review.dto.response.ReviewTodayTaskResp;
import com.study.module.system.review.entity.ReviewPlan;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 今日复习题单生成服务
 */
public interface ReviewTodayTaskListService {

    /**
     * 按总计划、科目额度和任务优先级生成题单，科目为空时生成全科题单。
     */
    List<ReviewTodayTaskResp> todayReviewTaskList(ReviewPlan reviewPlan, String subject,
                                                  LocalDate reviewDate,
                                                  LocalDateTime currentTime);
}
