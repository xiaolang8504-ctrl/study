package com.study.module.system.review.service.impl;

import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.msg.constants.Read;
import com.study.module.system.msg.entity.Msg;
import com.study.module.system.msg.service.MsgService;
import com.study.module.system.review.constants.ReviewReminderStatus;
import com.study.module.system.review.constants.ReviewReminderType;
import com.study.module.system.review.entity.ReviewPlan;
import com.study.module.system.review.entity.ReviewReminder;
import com.study.module.system.review.dto.response.ReviewTodayTaskResp;
import com.study.module.system.review.mapper.ReviewReminderMapper;
import com.study.module.system.review.service.ReviewReminderSendService;
import com.study.module.system.review.service.ReviewReminderService;
import com.study.module.system.review.service.ReviewTodayTaskListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 复习提醒单次发送服务实现
 */
@Service
public class ReviewReminderSendServiceImpl implements ReviewReminderSendService {

    private static final int MAX_RETRY_COUNT = 3;

    private static final int MAX_FAILURE_REASON_LENGTH = 500;

    // 同时兼容提醒表500字符和站内消息常见255字符限制，预留固定文案空间。
    private static final int MAX_SUBJECT_SUMMARY_LENGTH = 180;

    @Autowired
    ReviewTodayTaskListService reviewTodayTaskListService;

    @Autowired
    ReviewReminderService reviewReminderService;

    @Autowired
    ReviewReminderMapper reviewReminderMapper;

    @Autowired
    MsgService msgService;

    /**
     * 发送复习提醒
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendReviewReminder(ReviewPlan plan, LocalDate today, LocalDateTime now) {
        // 同一用户同一天只发送一次；达到最大重试次数后停止，避免定时任务反复打扰。
        ReviewReminder existingReminder = getReviewReminder(plan.getUserId(), today);
        if (existingReminder != null && (Integer.valueOf(ReviewReminderStatus.SENT)
                .equals(existingReminder.getSendStatus())
                || existingReminder.getRetryCount() != null
                && existingReminder.getRetryCount() >= MAX_RETRY_COUNT)) {
            return;
        }

        List<ReviewTodayTaskResp> taskList = reviewTodayTaskListService.todayReviewTaskList(
                plan, null, today, now);
        // 没有到期任务时不创建空消息，也不占用当天的提醒唯一键。
        if (taskList.isEmpty()) {
            return;
        }
        int visibleDueCount = taskList.size();
        long overdueCount = taskList.stream()
                .filter(task -> Integer.valueOf(1).equals(task.getOverdue()))
                .count();
        String subjectSummary = buildSubjectSummary(taskList);

        reviewReminderMapper.insertIgnoreReviewReminder(
                buildReminder(plan, today, visibleDueCount, overdueCount, subjectSummary, now));
        // 通过“先占提醒记录、再发消息”的方式抵御多个调度实例并发发送。
        ReviewReminder reminder = getReviewReminder(plan.getUserId(), today);
        if (reminder == null || Integer.valueOf(ReviewReminderStatus.SENT).equals(reminder.getSendStatus())) {
            return;
        }

        Msg msg = buildReviewMsg(plan.getUserId(), visibleDueCount, overdueCount,
                subjectSummary, now);
        if (!msgService.save(msg)) {
            throw new LogicException(ErrorCodeConstants.CREATE_MSG_FAIL);
        }
        // 消息保存成功后再把提醒置为已发送，保证提醒记录能够追溯到具体站内消息。
        boolean updated = reviewReminderService.lambdaUpdate()
                .eq(ReviewReminder::getId, reminder.getId())
                .ne(ReviewReminder::getSendStatus, ReviewReminderStatus.SENT)
                .set(ReviewReminder::getDueCount, visibleDueCount)
                .set(ReviewReminder::getOverdueCount, overdueCount)
                .set(ReviewReminder::getSubjectSummary, subjectSummary)
                .set(ReviewReminder::getMsgId, msg.getId())
                .set(ReviewReminder::getSendStatus, ReviewReminderStatus.SENT)
                .set(ReviewReminder::getSentTime, now)
                .set(ReviewReminder::getFailureReason, "")
                .set(ReviewReminder::getUpdateTime, now)
                .update();
        if (!updated) {
            throw new LogicException(ErrorCodeConstants.REVIEW_REMINDER_SEND_FAIL);
        }
    }

    /**
     * 记录提醒发送失败
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void recordSendFailure(ReviewPlan plan, LocalDate today, LocalDateTime now, Throwable throwable) {
        // 使用独立事务记录失败，即使外层发送事务回滚，调度器仍能看到重试次数和失败原因。
        reviewReminderMapper.insertIgnoreReviewReminder(buildReminder(plan, today, 0, 0, "", now));
        reviewReminderMapper.markReviewReminderFailed(plan.getUserId(), today,
                ReviewReminderType.DAILY_REVIEW, failureReason(throwable), now);
    }

    /**
     * 查询复习提醒
     */
    private ReviewReminder getReviewReminder(Long userId, LocalDate today) {
        return reviewReminderService.lambdaQuery()
                .eq(ReviewReminder::getUserId, userId)
                .eq(ReviewReminder::getReminderDate, today)
                .eq(ReviewReminder::getReminderType, ReviewReminderType.DAILY_REVIEW)
                .one();
    }

    /**
     * 构建复习提醒记录
     */
    private ReviewReminder buildReminder(ReviewPlan plan, LocalDate today, int dueCount,
                                         long overdueCount, String subjectSummary,
                                         LocalDateTime now) {
        ReviewReminder reminder = new ReviewReminder();
        reminder.setPlanId(plan.getId());
        reminder.setUserId(plan.getUserId());
        reminder.setReminderDate(today);
        reminder.setReminderType(ReviewReminderType.DAILY_REVIEW);
        reminder.setDueCount(dueCount);
        reminder.setOverdueCount((int) overdueCount);
        reminder.setSubjectSummary(subjectSummary);
        LocalTime reminderTime = plan.getReminderTime() == null ? now.toLocalTime() : plan.getReminderTime();
        reminder.setScheduledTime(LocalDateTime.of(today, reminderTime));
        reminder.setSendStatus(ReviewReminderStatus.PENDING);
        reminder.setRetryCount(0);
        reminder.setFailureReason("");
        reminder.setCreateTime(now);
        reminder.setUpdateTime(now);
        return reminder;
    }

    /**
     * 构建复习提醒消息
     */
    private Msg buildReviewMsg(Long userId, int dueCount, long overdueCount,
                               String subjectSummary, LocalDateTime now) {
        Msg msg = new Msg();
        msg.setMsgType(ReviewReminderType.DAILY_REVIEW);
        msg.setMsgTypeText("智能复习");
        msg.setMsgTitle("今日复习提醒");
        String overdueText = overdueCount > 0 ? "，其中" + overdueCount + "道已逾期" : "";
        String subjectText = subjectSummary.isEmpty() ? "" : "：" + subjectSummary;
        msg.setMsgContent("今天有" + dueCount + "道错题等待复习" + subjectText
                + overdueText + "，现在开始会更轻松。");
        msg.setIsRead(Read.NO);
        msg.setReceiveId(userId);
        msg.setCreateTime(now);
        msg.setUpdateTime(now);
        return msg;
    }

    /**
     * 构建科目摘要
     */
    private String buildSubjectSummary(List<ReviewTodayTaskResp> tasks) {
        // 按科目聚合数量，生成适合消息列表展示的短摘要，并主动截断防止字段溢出。
        Map<String, Long> subjectCountMap = tasks.stream().collect(Collectors.groupingBy(
                task -> task.getSubjectName() == null || task.getSubjectName().trim().isEmpty()
                        ? "未分类" : task.getSubjectName(),
                java.util.LinkedHashMap::new, Collectors.counting()));
        String summary = subjectCountMap.entrySet().stream()
                .map(entry -> entry.getKey() + entry.getValue() + "道")
                .collect(Collectors.joining("、"));
        return summary.length() > MAX_SUBJECT_SUMMARY_LENGTH
                ? summary.substring(0, MAX_SUBJECT_SUMMARY_LENGTH) : summary;
    }

    /**
     * 整理失败原因
     */
    private String failureReason(Throwable throwable) {
        // 异常可能没有 message，降级为异常类名并限制长度，避免失败记录本身再次写库失败。
        String message = throwable == null ? "未知异常" : throwable.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = throwable == null ? "未知异常" : throwable.getClass().getSimpleName();
        }
        return message.length() > MAX_FAILURE_REASON_LENGTH
                ? message.substring(0, MAX_FAILURE_REASON_LENGTH) : message;
    }
}
