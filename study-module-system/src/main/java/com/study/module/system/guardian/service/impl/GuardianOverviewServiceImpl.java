package com.study.module.system.guardian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.guardian.dto.response.GuardianStudentOverviewResp;
import com.study.module.system.guardian.dto.response.GuardianWeakPointResp;
import com.study.module.system.guardian.service.GuardianAccessAuditService;
import com.study.module.system.guardian.service.GuardianBindingService;
import com.study.module.system.guardian.service.GuardianOverviewService;
import com.study.module.system.review.constants.ReviewItemStatus;
import com.study.module.system.review.constants.ReviewMetricPolicy;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.review.service.ReviewRecordService;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.service.UserService;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 家长只读概览实现，先校验有效监护关系再查询学生聚合数据。 */
@Service
public class GuardianOverviewServiceImpl implements GuardianOverviewService {

    @Autowired
    private GuardianBindingService guardianBindingService;
    @Autowired
    private GuardianAccessAuditService guardianAccessAuditService;
    @Autowired
    private UserService userService;
    @Autowired
    private ReviewItemService reviewItemService;
    @Autowired
    private ReviewRecordService reviewRecordService;
    @Autowired
    private WrongQuestionService wrongQuestionService;

    @Override
    public GuardianStudentOverviewResp studentOverview(Long studentUserId) {
        return buildStudentOverview(studentUserId, "OVERVIEW_VIEWED");
    }

    @Override
    public GuardianStudentOverviewResp exportStudentOverview(Long studentUserId) {
        return buildStudentOverview(studentUserId, "OVERVIEW_EXPORTED");
    }

    private GuardianStudentOverviewResp buildStudentOverview(Long studentUserId, String auditAction) {
        if (!guardianBindingService.canCurrentGuardianAccessStudent(studentUserId)) {
            throw new LogicException(ErrorCodeConstants.GUARDIAN_BINDING_NOT_EXIST);
        }
        LocalDateTime now = LocalDateTime.now();
        List<ReviewItem> activeReviewItems = reviewItemService.list(new LambdaQueryWrapper<ReviewItem>()
                .eq(ReviewItem::getUserId, studentUserId)
                .eq(ReviewItem::getItemStatus, ReviewItemStatus.NORMAL));
        List<ReviewRecord> sevenDayRecords = reviewRecordService.list(new LambdaQueryWrapper<ReviewRecord>()
                .eq(ReviewRecord::getUserId, studentUserId)
                .ge(ReviewRecord::getReviewTime, now.minusDays(7))
                .lt(ReviewRecord::getReviewTime, now));
        List<WrongQuestion> questions = wrongQuestionService.list(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getCreateId, studentUserId));

        GuardianStudentOverviewResp response = new GuardianStudentOverviewResp();
        response.setStudentUserId(studentUserId);
        response.setStudentName(studentName(studentUserId));
        response.setTodayDueCount((int) activeReviewItems.stream().filter(item -> item.getNextReviewTime() != null
                && !item.getNextReviewTime().isAfter(now.toLocalDate().plusDays(1).atStartOfDay())).count());
        response.setOverdueCount((int) activeReviewItems.stream().filter(item -> item.getNextReviewTime() != null
                && item.getNextReviewTime().isBefore(now.toLocalDate().atStartOfDay())).count());
        response.setSevenDayReviewCount((int) sevenDayRecords.stream()
                .filter(ReviewMetricPolicy::isEffectiveReview).count());
        response.setWrongQuestionCount(questions.size());
        response.setCorrectionRate(correctionRate(questions));
        response.setIndependentCorrectRate(independentCorrectRate(sevenDayRecords));
        response.setWeakPointList(weakPoints(questions));
        response.setWeeklySuggestion(buildSuggestion(response));
        guardianAccessAuditService.record(null, studentUserId, AccountUtils.getUserId(), AccountUtils.getUserId(),
                auditAction, "SUCCESS", "OVERVIEW_EXPORTED".equals(auditAction) ? "导出学生只读学习汇总" : "查看学生只读学习概览");
        return response;
    }

    private String studentName(Long studentUserId) {
        User user = userService.getById(studentUserId);
        if (user == null) {
            throw new LogicException(ErrorCodeConstants.USER_NOT_EXIST);
        }
        return StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUserName();
    }

    private int correctionRate(List<WrongQuestion> questions) {
        if (questions.isEmpty()) {
            return 0;
        }
        long corrected = questions.stream().filter(question -> question.getStatus() != null
                && question.getStatus() >= WrongQuestionStatus.CORRECTED).count();
        return (int) Math.round(corrected * 100D / questions.size());
    }

    private int independentCorrectRate(List<ReviewRecord> records) {
        long judgedCount = records.stream().filter(ReviewMetricPolicy::isEffectiveReview).count();
        if (judgedCount == 0) {
            return 0;
        }
        long correctCount = records.stream().filter(ReviewMetricPolicy::isIndependentCorrect).count();
        return ReviewMetricPolicy.percent(correctCount, judgedCount);
    }

    private List<GuardianWeakPointResp> weakPoints(List<WrongQuestion> questions) {
        Map<String, List<WrongQuestion>> byPoint = questions.stream()
                .filter(question -> StringUtils.hasText(question.getLearningPoint()))
                .collect(Collectors.groupingBy(WrongQuestion::getLearningPoint, LinkedHashMap::new, Collectors.toList()));
        List<GuardianWeakPointResp> result = new ArrayList<>();
        for (Map.Entry<String, List<WrongQuestion>> entry : byPoint.entrySet()) {
            GuardianWeakPointResp point = new GuardianWeakPointResp();
            point.setLearningPoint(entry.getKey());
            point.setWrongQuestionCount(entry.getValue().size());
            point.setPendingCorrectionCount((int) entry.getValue().stream()
                    .filter(question -> Integer.valueOf(WrongQuestionStatus.PENDING_CORRECTION).equals(question.getStatus()))
                    .count());
            result.add(point);
        }
        return result.stream().sorted(Comparator.comparing(GuardianWeakPointResp::getPendingCorrectionCount).reversed()
                .thenComparing(GuardianWeakPointResp::getWrongQuestionCount, Comparator.reverseOrder()))
                .limit(5).collect(Collectors.toList());
    }

    private String buildSuggestion(GuardianStudentOverviewResp response) {
        if (response.getOverdueCount() > 0) {
            return "本周可先和孩子一起安排 " + response.getOverdueCount() + " 个逾期复习任务，避免继续积压。";
        }
        if (response.getCorrectionRate() < 60) {
            return "建议优先预留固定订正时间，完成订正后再安排新的专项练习。";
        }
        if (!response.getWeakPointList().isEmpty()) {
            return "可关注“" + response.getWeakPointList().get(0).getLearningPoint()
                    + "”的学习安排；本页仅展示汇总，不替代孩子作答。";
        }
        return "本周学习节奏稳定，可继续保持按时复习和订正的习惯。";
    }
}
