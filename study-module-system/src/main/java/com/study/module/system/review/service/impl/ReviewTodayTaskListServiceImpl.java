package com.study.module.system.review.service.impl;

import com.study.module.system.review.dto.response.ReviewTodayTaskResp;
import com.study.module.system.review.entity.ReviewPlan;
import com.study.module.system.review.entity.ReviewSubjectSetting;
import com.study.module.system.review.mapper.ReviewItemMapper;
import com.study.module.system.review.mapper.ReviewRecordMapper;
import com.study.module.system.review.service.ReviewSubjectSettingService;
import com.study.module.system.review.service.ReviewTodayTaskListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 今日复习题单生成服务实现
 */
@Service
public class ReviewTodayTaskListServiceImpl implements ReviewTodayTaskListService {

    @Autowired
    ReviewItemMapper reviewItemMapper;

    @Autowired
    ReviewRecordMapper reviewRecordMapper;

    @Autowired
    ReviewSubjectSettingService reviewSubjectSettingService;

    /**
     * 查询今日复习任务列表
     */
    @Override
    public List<ReviewTodayTaskResp> todayReviewTaskList(ReviewPlan reviewPlan, String subject,
                                                         LocalDate reviewDate,
                                                         LocalDateTime currentTime) {
        // 先扣除今天已经完成的题目，确保多次刷新或切换科目不会突破全局每日上限。
        LocalDateTime todayStart = reviewDate.atStartOfDay();
        LocalDateTime tomorrowStart = reviewDate.plusDays(1).atStartOfDay();
        long completedCount = reviewRecordMapper.countTodayCompleted(reviewPlan.getUserId(),
                todayStart, tomorrowStart);
        int globalRemaining = Math.max(0,
                reviewPlan.getDailyLimit() - Math.toIntExact(completedCount));
        if (globalRemaining == 0) {
            return new ArrayList<>();
        }

        String selectedSubject = StringUtils.hasText(subject) ? subject.trim() : null;
        List<ReviewTodayTaskResp> tasks = new ArrayList<>();
        // 每个科目先应用自己的启停状态和每日上限，再汇总到全局题单。
        for (ReviewSubjectSetting setting
                : reviewSubjectSettingService.effectiveSubjectSettings(reviewPlan)) {
            if (!Integer.valueOf(1).equals(setting.getEnabled())
                    || selectedSubject != null && !selectedSubject.equals(setting.getSubject())) {
                continue;
            }
            long completedSubjectCount = reviewRecordMapper.countTodayCompletedBySubject(
                    reviewPlan.getUserId(), setting.getSubject(), todayStart, tomorrowStart);
            int subjectRemaining = Math.max(0,
                    setting.getDailyLimit() - Math.toIntExact(completedSubjectCount));
            if (subjectRemaining > 0) {
                tasks.addAll(reviewItemMapper.todayReviewTaskList(reviewPlan.getUserId(),
                        setting.getSubject(), todayStart, tomorrowStart, currentTime,
                        subjectRemaining));
            }
        }
        // 汇总后重新排序并应用全局上限，防止先遍历到的科目长期占满题单。
        return tasks.stream().sorted(todayTaskComparator()).limit(globalRemaining)
                .collect(Collectors.toList());
    }

    /**
     * 构建今日任务排序规则
     */
    private Comparator<ReviewTodayTaskResp> todayTaskComparator() {
        // 复习优先级：逾期 > 难度 > 遗忘次数 > 最早到期，先处理最容易继续遗忘的题。
        return Comparator.comparing(ReviewTodayTaskResp::getOverdue,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ReviewTodayTaskResp::getLevel,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ReviewTodayTaskResp::getLapseCount,
                        Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ReviewTodayTaskResp::getNextReviewTime,
                        Comparator.nullsLast(Comparator.naturalOrder()));
    }
}
