package com.study.module.system.review.service.impl;

import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.dict.entity.DictData;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.review.dto.request.UpdateReviewPlanSettingReq;
import com.study.module.system.review.dto.request.ReviewSubjectSettingReq;
import com.study.module.system.review.dto.response.ReviewPlanSettingResp;
import com.study.module.system.review.dto.response.ReviewSubjectSettingResp;
import com.study.module.system.review.entity.ReviewPlan;
import com.study.module.system.review.entity.ReviewSubjectSetting;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.constants.ReviewItemStatus;
import com.study.module.system.review.mapper.ReviewSubjectSettingMapper;
import com.study.module.system.review.service.ReviewHomeService;
import com.study.module.system.review.service.ReviewPlanService;
import com.study.module.system.review.service.ReviewPlanSettingService;
import com.study.module.system.review.service.ReviewSubjectSettingService;
import com.study.module.system.review.service.ReviewEnrollmentService;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.wrongquestion.constants.WrongQuestionDictType;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 复习计划设置服务实现
 */
@Service
public class ReviewPlanSettingServiceImpl implements ReviewPlanSettingService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    ReviewPlanService reviewPlanService;

    @Autowired
    ReviewHomeService reviewHomeService;

    @Autowired
    ReviewSubjectSettingService reviewSubjectSettingService;

    @Autowired
    ReviewSubjectSettingMapper reviewSubjectSettingMapper;

    @Autowired
    DictDataService dictDataService;

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    ReviewEnrollmentService reviewEnrollmentService;

    @Autowired
    ReviewItemService reviewItemService;

    /**
     * 查询复习计划设置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewPlanSettingResp reviewPlanSetting() {
        Long userId = AccountUtils.getUserId();
        return buildResponse(getOrCreateReviewPlan(userId));
    }

    /**
     * 更新复习计划设置
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReviewPlanSetting(UpdateReviewPlanSettingReq request) {
        Long userId = AccountUtils.getUserId();
        ReviewPlan reviewPlan = getOrCreateReviewPlan(userId);

        // 星期去重并排序后再保存，保证后续排期与提醒任务读取到稳定格式。
        String reviewWeekDays = request.getReviewWeekDays().stream()
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        boolean updated = reviewPlanService.lambdaUpdate()
                .eq(ReviewPlan::getId, reviewPlan.getId())
                .eq(ReviewPlan::getUserId, userId)
                .set(ReviewPlan::getPlanName, request.getPlanName().trim())
                .set(ReviewPlan::getDailyLimit, request.getDailyLimit())
                .set(ReviewPlan::getReminderEnabled, request.getReminderEnabled())
                .set(ReviewPlan::getReminderTime, LocalTime.parse(request.getReminderTime(), TIME_FORMATTER))
                .set(ReviewPlan::getReviewWeekDays, reviewWeekDays)
                .set(ReviewPlan::getUpdateTime, LocalDateTime.now())
                .update();
        if (!updated) {
            throw new LogicException(ErrorCodeConstants.REVIEW_PLAN_SETTING_FAIL);
        }
        saveSubjectSettings(reviewPlan, userId, request.getSubjectSettings());
        syncReviewItems(userId, request.getSubjectSettings());
    }

    /**
     * 获取当前用户唯一计划；首次进入设置页时复用首页初始化逻辑创建默认计划。
     */
    private ReviewPlan getOrCreateReviewPlan(Long userId) {
        ReviewPlan reviewPlan = reviewPlanService.lambdaQuery()
                .eq(ReviewPlan::getUserId, userId)
                .one();
        if (reviewPlan == null) {
            reviewHomeService.initializeReviewHome();
            reviewPlan = reviewPlanService.lambdaQuery()
                    .eq(ReviewPlan::getUserId, userId)
                    .one();
        }
        if (reviewPlan == null) {
            throw new LogicException(ErrorCodeConstants.REVIEW_PLAN_NOT_EXIST);
        }
        return reviewPlan;
    }

    /**
     * 将数据库逗号分隔的星期配置转换为前端可直接绑定的数字数组。
     */
    private ReviewPlanSettingResp buildResponse(ReviewPlan reviewPlan) {
        ReviewPlanSettingResp response = new ReviewPlanSettingResp();
        response.setPlanId(reviewPlan.getId());
        response.setPlanName(reviewPlan.getPlanName());
        response.setDailyLimit(reviewPlan.getDailyLimit());
        response.setReminderEnabled(reviewPlan.getReminderEnabled());
        response.setReminderTime(reviewPlan.getReminderTime() == null
                ? null : reviewPlan.getReminderTime().format(TIME_FORMATTER));
        response.setReviewWeekDays(parseReviewWeekDays(reviewPlan.getReviewWeekDays()));
        response.setAlgorithmVersion(reviewPlan.getAlgorithmVersion());
        response.setSubjectSettings(buildSubjectSettings(reviewPlan));
        return response;
    }

    /**
     * 以启用的科目字典为展示基准，尚未保存的科目使用推荐默认值。
     */
    private List<ReviewSubjectSettingResp> buildSubjectSettings(ReviewPlan reviewPlan) {
        List<ReviewSubjectSettingResp> result = new ArrayList<>();
        for (ReviewSubjectSetting setting
                : reviewSubjectSettingService.effectiveSubjectSettings(reviewPlan)) {
            ReviewSubjectSettingResp response = new ReviewSubjectSettingResp();
            response.setSubject(setting.getSubject());
            response.setSubjectName(setting.getSubjectName());
            response.setEnabled(setting.getEnabled());
            response.setDailyLimit(setting.getDailyLimit());
            result.add(response);
        }
        return result;
    }

    /**
     * 校验科目字典并按计划、科目唯一键完成幂等保存。
     */
    private void saveSubjectSettings(ReviewPlan reviewPlan, Long userId,
                                     List<ReviewSubjectSettingReq> requests) {
        if (requests.stream().noneMatch(request -> Integer.valueOf(1).equals(request.getEnabled()))) {
            throw new LogicException(ErrorCodeConstants.REVIEW_PLAN_SETTING_FAIL);
        }
        Set<String> subjects = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();
        for (ReviewSubjectSettingReq request : requests) {
            String subject = request.getSubject().trim();
            if (!subjects.add(subject)) {
                throw new LogicException(ErrorCodeConstants.REVIEW_PLAN_SETTING_FAIL);
            }
            DictData subjectData = dictDataService.checkDictData(WrongQuestionDictType.SUBJECT,
                    subject, ErrorCodeConstants.INVALID_DICT_DATA_IDS);
            ReviewSubjectSetting setting = new ReviewSubjectSetting();
            setting.setPlanId(reviewPlan.getId());
            setting.setUserId(userId);
            setting.setSubject(subject);
            setting.setSubjectName(subjectData.getDictLabel());
            setting.setEnabled(request.getEnabled());
            setting.setDailyLimit(request.getDailyLimit());
            setting.setCreateTime(now);
            setting.setUpdateTime(now);
            if (reviewSubjectSettingMapper.insertOrUpdateReviewSubjectSetting(setting) < 1) {
                throw new LogicException(ErrorCodeConstants.REVIEW_PLAN_SETTING_FAIL);
            }
        }
    }

    /**
     * 设置保存后同步该用户可复习错题，使科目启停立即作用于任务队列。
     */
    private void syncReviewItems(Long userId, List<ReviewSubjectSettingReq> requests) {
        LocalDateTime now = LocalDateTime.now();
        for (ReviewSubjectSettingReq request : requests) {
            List<WrongQuestion> wrongQuestions = wrongQuestionService.lambdaQuery()
                    .eq(WrongQuestion::getCreateId, userId)
                    .eq(WrongQuestion::getSubject, request.getSubject().trim())
                    .in(WrongQuestion::getStatus, 1, 2)
                    .list();
            if (wrongQuestions.isEmpty()) {
                continue;
            }
            Set<Long> questionIds = wrongQuestions.stream()
                    .map(WrongQuestion::getId)
                    .collect(Collectors.toSet());
            Map<Long, ReviewItem> itemMap = reviewItemService.lambdaQuery()
                    .eq(ReviewItem::getUserId, userId)
                    .in(ReviewItem::getWrongQuestionId, questionIds)
                    .list().stream()
                    .collect(Collectors.toMap(ReviewItem::getWrongQuestionId,
                            Function.identity(), (first, second) -> first));
            for (WrongQuestion wrongQuestion : wrongQuestions) {
                ReviewItem reviewItem = itemMap.get(wrongQuestion.getId());
                if (reviewItem == null
                        || Integer.valueOf(ReviewItemStatus.FINISHED).equals(reviewItem.getItemStatus())) {
                    reviewEnrollmentService.syncWrongQuestionReview(wrongQuestion);
                    continue;
                }
                int targetStatus = Integer.valueOf(1).equals(request.getEnabled())
                        ? ReviewItemStatus.NORMAL : ReviewItemStatus.PAUSED;
                if (!Integer.valueOf(targetStatus).equals(reviewItem.getItemStatus())) {
                    reviewItemService.lambdaUpdate()
                            .eq(ReviewItem::getId, reviewItem.getId())
                            .eq(ReviewItem::getUserId, userId)
                            .set(ReviewItem::getItemStatus, targetStatus)
                            .set(ReviewItem::getUpdateTime, now)
                            .update();
                }
            }
        }
    }

    /**
     * 解析每周复习日
     */
    private List<Integer> parseReviewWeekDays(String reviewWeekDays) {
        if (reviewWeekDays == null || reviewWeekDays.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return Arrays.stream(reviewWeekDays.split(","))
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }
}
