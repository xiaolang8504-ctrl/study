package com.study.module.system.review.service.impl;

import com.study.module.system.review.constants.ReviewFeedback;
import com.study.module.system.review.constants.ReviewItemStatus;
import com.study.module.system.review.constants.ReviewPlanExplanationPolicy;
import com.study.module.system.review.dto.response.ReviewFutureLoadResp;
import com.study.module.system.review.dto.response.ReviewPlanExplanationResp;
import com.study.module.system.review.dto.response.ReviewTodayTaskResp;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.entity.ReviewPlan;
import com.study.module.system.review.entity.ReviewSubjectSetting;
import com.study.module.system.review.mapper.ReviewRecordMapper;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.review.service.ReviewPlanExplanationService;
import com.study.module.system.review.service.ReviewSubjectSettingService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 可解释复习计划服务实现。 */
@Service
public class ReviewPlanExplanationServiceImpl implements ReviewPlanExplanationService {

    private static final int MIN_ESTIMATED_MINUTES = 1;

    private static final int MAX_ESTIMATED_MINUTES = 10;

    private static final int FUTURE_LOAD_DAYS = 30;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("MM月dd日 HH:mm");

    @Autowired
    ReviewRecordMapper reviewRecordMapper;

    @Autowired
    ReviewItemService reviewItemService;

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    ReviewSubjectSettingService reviewSubjectSettingService;

    /**
     * 有真实主动回忆历史时采用平均用时，过短或没有样本时保持既有两分钟默认值。
     */
    @Override
    public int estimatedMinutesPerQuestion(Long userId, String subject) {
        double averageSeconds = StringUtils.hasText(subject)
                ? reviewRecordMapper.averageAnswerDurationBySubject(userId, subject.trim())
                : reviewRecordMapper.averageAnswerDuration(userId);
        if (averageSeconds < 30D) {
            return 2;
        }
        return Math.max(MIN_ESTIMATED_MINUTES, Math.min(MAX_ESTIMATED_MINUTES,
                (int) Math.ceil(averageSeconds / 60D)));
    }

    @Override
    public void explainTask(ReviewTodayTaskResp task, int estimatedMinutes,
                            LocalDate reviewDate, LocalDateTime currentTime) {
        task.setEstimatedMinutes(estimatedMinutes);
        task.setDueReason(dueReason(task, reviewDate, currentTime));
        task.setFeedbackProjectionList(Arrays.asList(
                ReviewPlanExplanationPolicy.project(task.getMasteryScore(), task.getStage(),
                        ReviewFeedback.FORGOT, currentTime),
                ReviewPlanExplanationPolicy.project(task.getMasteryScore(), task.getStage(),
                        ReviewFeedback.DIFFICULT, currentTime),
                ReviewPlanExplanationPolicy.project(task.getMasteryScore(), task.getStage(),
                        ReviewFeedback.MASTERED, currentTime),
                ReviewPlanExplanationPolicy.project(task.getMasteryScore(), task.getStage(),
                        ReviewFeedback.EASY, currentTime)));
    }

    @Override
    public ReviewPlanExplanationResp buildPlanExplanation(ReviewPlan reviewPlan, String subject,
                                                           LocalDate reviewDate,
                                                           List<ReviewTodayTaskResp> taskList,
                                                           int estimatedMinutesPerQuestion) {
        List<ReviewFutureLoadResp> allLoad = futureLoadList(reviewPlan, subject, reviewDate,
                estimatedMinutesPerQuestion);
        ReviewPlanExplanationResp response = new ReviewPlanExplanationResp();
        response.setTodayReason(todayReason(reviewPlan, taskList, estimatedMinutesPerQuestion));
        response.setEstimatedMinutesPerQuestion(estimatedMinutesPerQuestion);
        response.setTimeEstimateReason("预计用时优先采用已保存的主动回忆用时；样本不足或用时过短时"
                + "按每题约 2 分钟回退。当前按每题约 " + estimatedMinutesPerQuestion + " 分钟计算。");
        response.setThirtyDayLoadList(allLoad);
        response.setSevenDayLoadList(new ArrayList<>(allLoad.subList(0, Math.min(7, allLoad.size()))));
        response.setLoadNotice("负荷按当前到期时间计算；提交反馈、补做逾期题或调整每日上限后会自动重新安排。"
                + (StringUtils.hasText(subject) ? "当前仅展示所选科目。" : ""));
        return response;
    }

    /** 今天进入题单的原因必须由当前到期时间和已有复习证据推导。 */
    private String dueReason(ReviewTodayTaskResp task, LocalDate reviewDate,
                             LocalDateTime currentTime) {
        LocalDateTime nextReviewTime = task.getNextReviewTime();
        if (nextReviewTime == null) {
            return "该题尚未形成完整排期，系统将其放入今日任务优先补齐复习证据。";
        }
        if (Integer.valueOf(1).equals(task.getOverdue())) {
            long hours = Math.max(1, Duration.between(nextReviewTime, currentTime).toHours());
            return "原定 " + TIME_FORMATTER.format(nextReviewTime) + " 复习，已逾期 "
                    + durationLabel(hours) + "；按逾期优先进入今日题单。";
        }
        if (!nextReviewTime.isAfter(currentTime)) {
            return "已到计划复习时间（" + TIME_FORMATTER.format(nextReviewTime)
                    + "），这是第 " + Math.max(1, value(task.getReviewCount()) + 1) + " 次巩固。";
        }
        if (reviewDate.equals(nextReviewTime.toLocalDate())) {
            return "计划在今天 " + nextReviewTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                    + " 到期；可提前完成，提交反馈后会按实际作答重新安排。";
        }
        return "当前间隔为 " + intervalLabel(task.getCurrentIntervalMinutes())
                + "，系统根据到期时间将其列入今日任务。";
    }

    private List<ReviewFutureLoadResp> futureLoadList(ReviewPlan reviewPlan, String subject,
                                                       LocalDate today,
                                                       int estimatedMinutesPerQuestion) {
        Map<LocalDate, Integer> dueCountMap = new LinkedHashMap<>();
        for (int index = 0; index < FUTURE_LOAD_DAYS; index++) {
            dueCountMap.put(today.plusDays(index), 0);
        }
        List<ReviewItem> activeItems = reviewItemService.lambdaQuery()
                .eq(ReviewItem::getPlanId, reviewPlan.getId())
                .eq(ReviewItem::getUserId, reviewPlan.getUserId())
                .eq(ReviewItem::getItemStatus, ReviewItemStatus.NORMAL)
                .list();
        Map<Long, WrongQuestion> questionMap = questionMap(reviewPlan.getUserId(), activeItems);
        Set<String> enabledSubjects = reviewSubjectSettingService.effectiveSubjectSettings(reviewPlan)
                .stream().filter(setting -> Integer.valueOf(1).equals(setting.getEnabled()))
                .map(ReviewSubjectSetting::getSubject).collect(Collectors.toSet());
        String selectedSubject = StringUtils.hasText(subject) ? subject.trim() : null;
        for (ReviewItem item : activeItems) {
            WrongQuestion question = questionMap.get(item.getWrongQuestionId());
            if (question == null || !enabledSubjects.contains(question.getSubject())
                    || selectedSubject != null && !selectedSubject.equals(question.getSubject())
                    || item.getNextReviewTime() == null) {
                continue;
            }
            LocalDate dueDate = item.getNextReviewTime().toLocalDate();
            if (dueDate.isBefore(today)) {
                dueDate = today;
            }
            if (dueCountMap.containsKey(dueDate)) {
                dueCountMap.put(dueDate, dueCountMap.get(dueDate) + 1);
            }
        }
        List<ReviewFutureLoadResp> response = new ArrayList<>();
        dueCountMap.forEach((date, dueCount) -> {
            ReviewFutureLoadResp load = new ReviewFutureLoadResp();
            load.setReviewDate(date);
            load.setDueCount(dueCount);
            load.setEstimatedMinutes(dueCount * estimatedMinutesPerQuestion);
            response.add(load);
        });
        return response;
    }

    private Map<Long, WrongQuestion> questionMap(Long userId, List<ReviewItem> items) {
        if (items.isEmpty()) {
            return new HashMap<>();
        }
        Set<Long> questionIds = items.stream().map(ReviewItem::getWrongQuestionId)
                .collect(Collectors.toSet());
        return wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getCreateId, userId)
                .in(WrongQuestion::getId, questionIds)
                .list().stream().collect(Collectors.toMap(WrongQuestion::getId, item -> item));
    }

    private String todayReason(ReviewPlan reviewPlan, List<ReviewTodayTaskResp> taskList,
                               int estimatedMinutesPerQuestion) {
        long overdue = taskList.stream().filter(item -> Integer.valueOf(1).equals(item.getOverdue())).count();
        String reason = "今日安排 " + taskList.size() + " 道，受每日上限 " + reviewPlan.getDailyLimit()
                + " 道和当前到期顺序共同约束，预计约 "
                + taskList.size() * estimatedMinutesPerQuestion + " 分钟。";
        return overdue > 0 ? reason + "其中 " + overdue + " 道已逾期，会优先处理。" : reason;
    }

    private int value(Integer number) {
        return number == null ? 0 : number;
    }

    private String durationLabel(long hours) {
        if (hours >= 24) {
            return hours / 24 + " 天" + (hours % 24 == 0 ? "" : " " + hours % 24 + " 小时");
        }
        return hours + " 小时";
    }

    private String intervalLabel(Integer intervalMinutes) {
        int safeMinutes = intervalMinutes == null ? 0 : intervalMinutes;
        if (safeMinutes >= 1440) {
            return safeMinutes / 1440 + " 天";
        }
        return Math.max(1, safeMinutes) + " 分钟";
    }
}
