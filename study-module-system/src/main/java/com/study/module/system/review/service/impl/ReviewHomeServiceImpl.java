package com.study.module.system.review.service.impl;

import com.study.module.system.review.constants.ReviewDefault;
import com.study.module.system.review.constants.ReviewItemStatus;
import com.study.module.system.review.constants.ReviewPlanStatus;
import com.study.module.system.review.constants.ReviewStage;
import com.study.module.system.review.dto.response.ReviewHomeResp;
import com.study.module.system.review.dto.response.ReviewLearningPointResp;
import com.study.module.system.review.dto.response.ReviewScheduleResp;
import com.study.module.system.review.dto.response.ReviewSubjectSettingResp;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.entity.ReviewPlan;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.entity.ReviewSubjectSetting;
import com.study.module.system.review.mapper.ReviewItemMapper;
import com.study.module.system.review.mapper.ReviewPlanMapper;
import com.study.module.system.review.service.ReviewHomeService;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.review.service.ReviewPlanService;
import com.study.module.system.review.service.ReviewRecordService;
import com.study.module.system.review.service.ReviewSubjectSettingService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 智能复习首页服务实现
 */
@Service
public class ReviewHomeServiceImpl implements ReviewHomeService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private static final int SCHEDULE_DAYS = 7;

    private static final int CONTINUOUS_DAY_QUERY_LIMIT = 366;

    private static final int INITIAL_MASTERY_SCORE = 40;

    private static final int MASTERED_MASTERY_SCORE = 90;

    @Autowired
    ReviewPlanService reviewPlanService;

    @Autowired
    ReviewPlanMapper reviewPlanMapper;

    @Autowired
    ReviewItemService reviewItemService;

    @Autowired
    ReviewItemMapper reviewItemMapper;

    @Autowired
    ReviewRecordService reviewRecordService;

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    ReviewSubjectSettingService reviewSubjectSettingService;

    /**
     * 初始化复习首页
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewHomeResp initializeReviewHome() {
        return initializeReviewHome(null);
    }

    /**
     * 初始化复习首页
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewHomeResp initializeReviewHome(String subject) {
        Long userId = AccountUtils.getUserId();
        LocalDateTime now = LocalDateTime.now();
        // 首次访问时惰性创建计划，并补齐“已订正但尚未入计划”的历史错题。
        ReviewPlan plan = getOrCreateReviewPlan(userId, now);
        initializeReviewItems(userId, plan, now);
        // 科目必须属于当前计划的启用范围，防止构造参数越过科目设置。
        String selectedSubject = normalizeAndValidateSubject(plan, subject);
        return buildReviewHome(userId, plan, selectedSubject);
    }

    /**
     * 查询或创建复习计划
     */
    private ReviewPlan getOrCreateReviewPlan(Long userId, LocalDateTime now) {
        ReviewPlan plan = reviewPlanService.lambdaQuery()
                .eq(ReviewPlan::getUserId, userId)
                .one();
        if (plan != null) {
            return plan;
        }

        // 数据库唯一索引配合 insertIgnore，允许首页并发请求安全地初始化同一个计划。
        ReviewPlan newPlan = new ReviewPlan();
        newPlan.setUserId(userId);
        newPlan.setPlanName(ReviewDefault.PLAN_NAME);
        newPlan.setDailyLimit(ReviewDefault.DAILY_LIMIT);
        newPlan.setReminderEnabled(ReviewDefault.REMINDER_ENABLED);
        newPlan.setReminderTime(ReviewDefault.REMINDER_TIME);
        newPlan.setReviewWeekDays(ReviewDefault.REVIEW_WEEK_DAYS);
        newPlan.setAlgorithmVersion(ReviewDefault.ALGORITHM_VERSION);
        newPlan.setStatus(ReviewPlanStatus.ENABLED);
        newPlan.setCreateTime(now);
        newPlan.setUpdateTime(now);
        reviewPlanMapper.insertIgnoreReviewPlan(newPlan);
        return reviewPlanService.lambdaQuery()
                .eq(ReviewPlan::getUserId, userId)
                .one();
    }

    /**
     * 初始化复习项
     */
    private void initializeReviewItems(Long userId, ReviewPlan plan, LocalDateTime now) {
        // 先收集已经入计划的错题 ID，只为缺失项补数据，避免每次打开首页重复插入。
        Set<Long> initializedQuestionIds = reviewItemService.lambdaQuery()
                .select(ReviewItem::getWrongQuestionId)
                .eq(ReviewItem::getUserId, userId)
                .list()
                .stream()
                .map(ReviewItem::getWrongQuestionId)
                .collect(Collectors.toSet());

        // 待订正题答案尚未完整，不进入复习；只同步已订正和已掌握题。
        List<WrongQuestion> wrongQuestionList = wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getCreateId, userId)
                .in(WrongQuestion::getStatus, 1, 2)
                .list();
        Map<String, Boolean> subjectEnabledMap = reviewSubjectSettingService
                .effectiveSubjectSettings(plan).stream()
                .collect(Collectors.toMap(ReviewSubjectSetting::getSubject,
                        setting -> Integer.valueOf(1).equals(setting.getEnabled())));
        for (WrongQuestion wrongQuestion : wrongQuestionList) {
            if (initializedQuestionIds.contains(wrongQuestion.getId())) {
                continue;
            }
            LocalDateTime baseTime = wrongQuestion.getUpdateTime() == null ? now : wrongQuestion.getUpdateTime();
            ReviewItem item = buildInitialReviewItem(plan.getId(), userId, wrongQuestion, baseTime);
            if (!subjectEnabledMap.getOrDefault(wrongQuestion.getSubject(), false)) {
                item.setItemStatus(ReviewItemStatus.PAUSED);
            }
            reviewItemMapper.insertIgnoreReviewItem(item);
        }
    }

    /**
     * 构建初始复习项
     */
    private ReviewItem buildInitialReviewItem(Long planId, Long userId, WrongQuestion wrongQuestion,
                                              LocalDateTime baseTime) {
        // 老数据若已标记掌握，直接从掌握阶段开始；其他题从初始间隔开始。
        boolean mastered = Integer.valueOf(2).equals(wrongQuestion.getStatus());
        int intervalMinutes = mastered
                ? ReviewDefault.MASTERED_INTERVAL_MINUTES : ReviewDefault.INITIAL_INTERVAL_MINUTES;

        ReviewItem item = new ReviewItem();
        item.setPlanId(planId);
        item.setUserId(userId);
        item.setWrongQuestionId(wrongQuestion.getId());
        item.setItemStatus(ReviewItemStatus.NORMAL);
        item.setStage(mastered ? ReviewDefault.MASTERED_STAGE : ReviewStage.INITIAL);
        item.setCurrentIntervalMinutes(intervalMinutes);
        item.setMasteryScore(mastered ? MASTERED_MASTERY_SCORE : INITIAL_MASTERY_SCORE);
        item.setCorrectStreak(0);
        item.setWrongStreak(0);
        item.setLapseCount(0);
        item.setReviewCount(0);
        item.setNextReviewTime(baseTime.plusMinutes(intervalMinutes));
        item.setMasteredTime(mastered ? baseTime : null);
        item.setAlgorithmVersion(ReviewDefault.ALGORITHM_VERSION);
        item.setVersion(0);
        item.setCreateTime(baseTime);
        item.setUpdateTime(baseTime);
        return item;
    }

    /**
     * 构建复习首页数据
     */
    private ReviewHomeResp buildReviewHome(Long userId, ReviewPlan plan, String selectedSubject) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        // 一次加载有效复习项和近七日记录，后续所有卡片指标基于同一份数据计算。
        List<ReviewItem> allActiveItems = reviewItemService.lambdaQuery()
                .eq(ReviewItem::getUserId, userId)
                .eq(ReviewItem::getItemStatus, ReviewItemStatus.NORMAL)
                .list();
        List<ReviewRecord> allRecentRecords = reviewRecordService.lambdaQuery()
                .eq(ReviewRecord::getUserId, userId)
                .ge(ReviewRecord::getReviewTime, todayStart)
                .lt(ReviewRecord::getReviewTime, today.plusDays(SCHEDULE_DAYS).atStartOfDay())
                .list();
        List<ReviewItem> enabledActiveItems = filterItemsByEnabledSubjects(
                plan, allActiveItems);
        List<ReviewItem> activeItems = filterItemsBySubject(enabledActiveItems, selectedSubject);
        List<ReviewRecord> recentRecords = filterRecordsBySubject(allRecentRecords, selectedSubject);

        // 同一道题当天多次反馈只算一次完成，避免重复练习抬高今日进度。
        long completedCount = recentRecords.stream()
                .filter(record -> record.getReviewTime() != null
                        && today.equals(record.getReviewTime().toLocalDate()))
                .map(ReviewRecord::getReviewItemId)
                .distinct()
                .count();
        long globalCompletedCount = allRecentRecords.stream()
                .filter(record -> record.getReviewTime() != null
                        && today.equals(record.getReviewTime().toLocalDate()))
                .map(ReviewRecord::getReviewItemId)
                .distinct()
                .count();
        long subjectAvailableCount = calculateSubjectAvailableCount(
                plan, activeItems, recentRecords, today, todayStart,
                tomorrowStart, LocalDateTime.now());
        // 科目可用数量仍需受全局每日上限约束，保证切换科目后总题量不超限。
        long remainingCount = Math.min(subjectAvailableCount,
                Math.max(0L, plan.getDailyLimit().longValue() - globalCompletedCount));
        long overdueCount = activeItems.stream()
                .filter(item -> item.getNextReviewTime() != null
                        && item.getNextReviewTime().isBefore(todayStart))
                .count();

        // 总体掌握率与学情报告保持同一分母：当前学生的全部错题（含待订正及归档）。
        long reviewQuestionCount = wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getCreateId, userId)
                .eq(StringUtils.hasText(selectedSubject), WrongQuestion::getSubject,
                        selectedSubject)
                .count();
        long masteredCount = wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getCreateId, userId)
                .eq(StringUtils.hasText(selectedSubject), WrongQuestion::getSubject,
                        selectedSubject)
                .eq(WrongQuestion::getStatus, 2)
                .count();

        // 最后统一组装首页，确保统计、排期、知识点和科目设置使用同一筛选条件。
        ReviewHomeResp response = new ReviewHomeResp();
        response.setPlanId(plan.getId());
        response.setPlanName(plan.getPlanName());
        response.setDailyLimit(resolveDailyLimit(plan, selectedSubject));
        response.setReminderEnabled(plan.getReminderEnabled());
        response.setReminderTime(plan.getReminderTime() == null
                ? null : plan.getReminderTime().format(TIME_FORMATTER));
        response.setTodayTaskCount(completedCount + remainingCount);
        response.setCompletedCount(completedCount);
        response.setRemainingCount(remainingCount);
        response.setOverdueCount(overdueCount);
        response.setEstimatedMinutes(Math.toIntExact(remainingCount * ReviewDefault.ESTIMATED_MINUTES_PER_QUESTION));
        response.setContinuousReviewDays(calculateContinuousReviewDays(userId, today));
        response.setReviewQuestionCount(reviewQuestionCount);
        response.setMasteredCount(masteredCount);
        response.setMasteryRate(calculateRate(masteredCount, reviewQuestionCount));
        response.setAverageMasteryScore(calculateAverageMasteryScore(activeItems));
        response.setScheduleList(buildScheduleList(today, activeItems, recentRecords));
        response.setLearningPointList(buildLearningPointList(activeItems));
        response.setSelectedSubject(selectedSubject);
        response.setSubjectSettings(buildSubjectSettingResponses(plan));
        return response;
    }

    /**
     * 规范化并校验科目
     */
    private String normalizeAndValidateSubject(ReviewPlan plan, String subject) {
        if (!StringUtils.hasText(subject)) {
            return null;
        }
        String normalizedSubject = subject.trim();
        boolean valid = reviewSubjectSettingService.effectiveSubjectSettings(plan).stream()
                .anyMatch(setting -> normalizedSubject.equals(setting.getSubject())
                        && Integer.valueOf(1).equals(setting.getEnabled()));
        if (!valid) {
            throw new LogicException(ErrorCodeConstants.INVALID_DICT_DATA_IDS);
        }
        return normalizedSubject;
    }

    /**
     * 计算每日复习上限
     */
    private int resolveDailyLimit(ReviewPlan plan, String selectedSubject) {
        if (!StringUtils.hasText(selectedSubject)) {
            return plan.getDailyLimit();
        }
        return reviewSubjectSettingService.effectiveSubjectSettings(plan).stream()
                .filter(setting -> selectedSubject.equals(setting.getSubject()))
                .map(ReviewSubjectSetting::getDailyLimit)
                .findFirst().orElse(plan.getDailyLimit());
    }

    /**
     * 构建科目设置响应
     */
    private List<ReviewSubjectSettingResp> buildSubjectSettingResponses(ReviewPlan plan) {
        return reviewSubjectSettingService.effectiveSubjectSettings(plan).stream()
                .filter(setting -> Integer.valueOf(1).equals(setting.getEnabled()))
                .map(setting -> {
                    ReviewSubjectSettingResp response = new ReviewSubjectSettingResp();
                    response.setSubject(setting.getSubject());
                    response.setSubjectName(setting.getSubjectName());
                    response.setEnabled(setting.getEnabled());
                    response.setDailyLimit(setting.getDailyLimit());
                    return response;
                }).collect(Collectors.toList());
    }

    /**
     * 按科目筛选复习项
     */
    private List<ReviewItem> filterItemsBySubject(List<ReviewItem> items, String selectedSubject) {
        if (!StringUtils.hasText(selectedSubject) || items.isEmpty()) {
            return items;
        }
        Set<Long> questionIds = items.stream().map(ReviewItem::getWrongQuestionId)
                .collect(Collectors.toSet());
        Set<Long> selectedQuestionIds = wrongQuestionService.lambdaQuery()
                .select(WrongQuestion::getId)
                .in(WrongQuestion::getId, questionIds)
                .eq(WrongQuestion::getSubject, selectedSubject)
                .list().stream().map(WrongQuestion::getId).collect(Collectors.toSet());
        return items.stream().filter(item -> selectedQuestionIds.contains(item.getWrongQuestionId()))
                .collect(Collectors.toList());
    }

    /**
     * 按已启用科目筛选复习项
     */
    private List<ReviewItem> filterItemsByEnabledSubjects(ReviewPlan plan, List<ReviewItem> items) {
        if (items.isEmpty()) {
            return items;
        }
        Set<String> enabledSubjects = reviewSubjectSettingService.effectiveSubjectSettings(plan)
                .stream()
                .filter(setting -> Integer.valueOf(1).equals(setting.getEnabled()))
                .map(ReviewSubjectSetting::getSubject)
                .collect(Collectors.toSet());
        if (enabledSubjects.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> questionIds = items.stream().map(ReviewItem::getWrongQuestionId)
                .collect(Collectors.toSet());
        Set<Long> enabledQuestionIds = wrongQuestionService.lambdaQuery()
                .select(WrongQuestion::getId)
                .in(WrongQuestion::getId, questionIds)
                .in(WrongQuestion::getSubject, enabledSubjects)
                .list().stream().map(WrongQuestion::getId).collect(Collectors.toSet());
        return items.stream().filter(item -> enabledQuestionIds.contains(item.getWrongQuestionId()))
                .collect(Collectors.toList());
    }

    /**
     * 按科目筛选复习记录
     */
    private List<ReviewRecord> filterRecordsBySubject(List<ReviewRecord> records, String selectedSubject) {
        if (!StringUtils.hasText(selectedSubject) || records.isEmpty()) {
            return records;
        }
        return records.stream().filter(record -> selectedSubject.equals(record.getSubject()))
                .collect(Collectors.toList());
    }

    /**
     * 计算连续复习天数
     */
    private Integer calculateContinuousReviewDays(Long userId, LocalDate today) {
        List<ReviewRecord> recordList = reviewRecordService.lambdaQuery()
                .select(ReviewRecord::getReviewTime)
                .eq(ReviewRecord::getUserId, userId)
                .orderByDesc(ReviewRecord::getReviewTime)
                .last("LIMIT " + CONTINUOUS_DAY_QUERY_LIMIT)
                .list();
        Set<LocalDate> reviewDates = recordList.stream()
                .filter(record -> record.getReviewTime() != null)
                .map(record -> record.getReviewTime().toLocalDate())
                .collect(Collectors.toSet());
        LocalDate cursor = reviewDates.contains(today) ? today : today.minusDays(1);
        int continuousDays = 0;
        while (reviewDates.contains(cursor)) {
            continuousDays++;
            cursor = cursor.minusDays(1);
        }
        return continuousDays;
    }

    /**
     * 按科目每日上限扣除当日已完成题目，计算当前还能进入今日任务的到期题量。
     */
    private long calculateSubjectAvailableCount(ReviewPlan plan, List<ReviewItem> activeItems,
                                                List<ReviewRecord> recentRecords, LocalDate today,
                                                LocalDateTime todayStart,
                                                LocalDateTime tomorrowStart,
                                                LocalDateTime currentTime) {
        Set<Long> questionIds = activeItems.stream()
                .map(ReviewItem::getWrongQuestionId)
                .collect(Collectors.toSet());
        if (questionIds.isEmpty()) {
            return 0;
        }
        Map<Long, String> questionSubjectMap = wrongQuestionService.listByIds(questionIds).stream()
                .collect(Collectors.toMap(WrongQuestion::getId,
                        question -> question.getSubject() == null ? "" : question.getSubject()));
        Map<String, Long> dueCountMap = activeItems.stream()
                .filter(item -> item.getNextReviewTime() != null
                        && item.getNextReviewTime().isBefore(tomorrowStart)
                        && (item.getLastReviewTime() == null
                        || item.getLastReviewTime().isBefore(todayStart)
                        || !item.getNextReviewTime().isAfter(currentTime)))
                .collect(Collectors.groupingBy(
                        item -> questionSubjectMap.getOrDefault(item.getWrongQuestionId(), ""),
                        Collectors.counting()));
        Map<String, Long> completedCountMap = recentRecords.stream()
                .filter(record -> record.getReviewTime() != null
                        && today.equals(record.getReviewTime().toLocalDate()))
                .collect(Collectors.groupingBy(
                        record -> record.getSubject() == null ? "" : record.getSubject(),
                        Collectors.mapping(ReviewRecord::getReviewItemId,
                                Collectors.collectingAndThen(Collectors.toSet(), set -> (long) set.size()))));
        Map<String, ReviewSubjectSetting> settingMap = reviewSubjectSettingService
                .effectiveSubjectSettings(plan).stream()
                .collect(Collectors.toMap(ReviewSubjectSetting::getSubject, setting -> setting));
        long availableCount = 0;
        for (Map.Entry<String, Long> entry : dueCountMap.entrySet()) {
            ReviewSubjectSetting setting = settingMap.get(entry.getKey());
            if (setting == null || !Integer.valueOf(1).equals(setting.getEnabled())) {
                continue;
            }
            long dailyLimit = setting.getDailyLimit();
            long subjectRemaining = Math.max(0L,
                    dailyLimit - completedCountMap.getOrDefault(entry.getKey(), 0L));
            availableCount += Math.min(entry.getValue(), subjectRemaining);
        }
        return availableCount;
    }

    /**
     * 构建复习日程列表
     */
    private List<ReviewScheduleResp> buildScheduleList(LocalDate today, List<ReviewItem> activeItems,
                                                        List<ReviewRecord> recentRecords) {
        Map<LocalDate, ReviewScheduleResp> scheduleMap = new LinkedHashMap<>();
        for (int day = 0; day < SCHEDULE_DAYS; day++) {
            ReviewScheduleResp schedule = new ReviewScheduleResp();
            schedule.setReviewDate(today.plusDays(day));
            schedule.setDueCount(0L);
            schedule.setCompletedCount(0L);
            scheduleMap.put(schedule.getReviewDate(), schedule);
        }

        for (ReviewItem item : activeItems) {
            if (item.getNextReviewTime() == null) {
                continue;
            }
            LocalDate reviewDate = item.getNextReviewTime().toLocalDate();
            if (reviewDate.isBefore(today)) {
                reviewDate = today;
            }
            ReviewScheduleResp schedule = scheduleMap.get(reviewDate);
            if (schedule != null) {
                schedule.setDueCount(schedule.getDueCount() + 1);
            }
        }

        Map<LocalDate, Long> completedCountMap = recentRecords.stream()
                .filter(record -> record.getReviewTime() != null)
                .collect(Collectors.groupingBy(record -> record.getReviewTime().toLocalDate(),
                        Collectors.mapping(ReviewRecord::getReviewItemId,
                                Collectors.collectingAndThen(Collectors.toSet(), set -> (long) set.size()))));
        completedCountMap.forEach((reviewDate, count) -> {
            ReviewScheduleResp schedule = scheduleMap.get(reviewDate);
            if (schedule != null) {
                schedule.setCompletedCount(count);
            }
        });
        return new ArrayList<>(scheduleMap.values());
    }

    /**
     * 构建知识点列表
     */
    private List<ReviewLearningPointResp> buildLearningPointList(List<ReviewItem> activeItems) {
        if (activeItems.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> wrongQuestionIds = activeItems.stream()
                .map(ReviewItem::getWrongQuestionId)
                .collect(Collectors.toSet());
        List<WrongQuestion> wrongQuestionList = wrongQuestionService.listByIds(wrongQuestionIds);
        Map<String, List<WrongQuestion>> learningPointMap = wrongQuestionList.stream()
                .collect(Collectors.groupingBy(question -> StringUtils.hasText(question.getLearningPoint())
                        ? question.getLearningPoint().trim() : "未分类"));

        List<ReviewLearningPointResp> result = new ArrayList<>();
        learningPointMap.forEach((learningPoint, questions) -> {
            long masteredCount = questions.stream()
                    .filter(question -> Integer.valueOf(2).equals(question.getStatus()))
                    .count();
            ReviewLearningPointResp response = new ReviewLearningPointResp();
            response.setLearningPoint(learningPoint);
            response.setQuestionCount((long) questions.size());
            response.setMasteredCount(masteredCount);
            response.setMasteryRate(calculateRate(masteredCount, questions.size()));
            response.setAverageMasteryScore(calculateAverageMasteryScoreByQuestions(
                    activeItems, questions));
            result.add(response);
        });
        return result.stream()
                .sorted(Comparator.comparing(ReviewLearningPointResp::getQuestionCount).reversed()
                        .thenComparing(ReviewLearningPointResp::getLearningPoint))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 计算比率
     */
    private BigDecimal calculateRate(long numerator, long denominator) {
        if (denominator == 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator * 100)
                .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP);
    }

    /**
     * 计算复习项平均掌握度。
     */
    private BigDecimal calculateAverageMasteryScore(List<ReviewItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        double average = items.stream()
                .mapToInt(item -> item.getMasteryScore() == null
                        ? INITIAL_MASTERY_SCORE : item.getMasteryScore())
                .average().orElse(0);
        return BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 按知识点题目集合计算平均掌握度。
     */
    private BigDecimal calculateAverageMasteryScoreByQuestions(List<ReviewItem> items,
                                                               List<WrongQuestion> questions) {
        Set<Long> questionIds = questions.stream()
                .map(WrongQuestion::getId)
                .collect(Collectors.toSet());
        List<ReviewItem> matchedItems = items.stream()
                .filter(item -> questionIds.contains(item.getWrongQuestionId()))
                .collect(Collectors.toList());
        return calculateAverageMasteryScore(matchedItems);
    }
}
