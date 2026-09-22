package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.review.dto.request.ReviewHistoryPageListReq;
import com.study.module.system.review.dto.response.ReviewHistoryHomeResp;
import com.study.module.system.review.dto.response.ReviewHistoryPageListResp;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.service.ReviewHistoryListService;
import com.study.module.system.review.service.ReviewRecordService;
import com.study.module.system.review.mapper.ReviewRecordMapper;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.stream.Collectors;

/**
 * 复习历史列表服务实现
 */
@Service
public class ReviewHistoryListServiceImpl implements ReviewHistoryListService {

    @Autowired
    ReviewRecordService reviewRecordService;

    @Autowired
    ReviewRecordMapper reviewRecordMapper;

    /**
     * 分页查询复习历史
     */
    @Override
    public ReviewHistoryHomeResp reviewHistoryPageList(ReviewHistoryPageListReq request) {
        Long userId = AccountUtils.getUserId();
        // 分页明细与顶部统计共用当前用户和科目口径，避免页面指标与列表互相矛盾。
        LambdaQueryWrapper<ReviewRecord> queryWrapper = buildQueryWrapper(request, userId);
        Page<ReviewRecord> page = new Page<>(request.getCurrent(), request.getPageSize());
        reviewRecordService.page(page, queryWrapper);

        ReviewHistoryHomeResp response = new ReviewHistoryHomeResp();
        response.setTotalReviewCount(reviewRecordService.lambdaQuery()
                .eq(ReviewRecord::getUserId, userId)
                .eq(StringUtils.hasText(request.getSubject()), ReviewRecord::getSubject,
                        request.getSubject())
                .count());
        // “本周”固定从周一零点开始计算，符合学习周报和前端展示口径。
        LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
        response.setWeekReviewCount(reviewRecordService.lambdaQuery()
                .eq(ReviewRecord::getUserId, userId)
                .eq(StringUtils.hasText(request.getSubject()), ReviewRecord::getSubject,
                        request.getSubject())
                .ge(ReviewRecord::getReviewTime, weekStart.atStartOfDay()).count());
        fillHistoryStatistics(response, userId, request.getSubject(),
                response.getTotalReviewCount());
        response.setPageResult(PageUtils.wrap(page, records -> records.stream()
                .map(this::buildPageResponse)
                .collect(Collectors.toList())));
        return response;
    }

    /**
     * 查询条件始终包含当前用户，防止通过分页参数读取其他学生的历史。
     */
    private LambdaQueryWrapper<ReviewRecord> buildQueryWrapper(ReviewHistoryPageListReq request, Long userId) {
        LambdaQueryWrapper<ReviewRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewRecord::getUserId, userId);
        wrapper.eq(StringUtils.hasText(request.getSubject()), ReviewRecord::getSubject,
                request.getSubject());
        wrapper.eq(request.getFeedback() != null, ReviewRecord::getFeedback, request.getFeedback());
        wrapper.ge(request.getStartDate() != null, ReviewRecord::getReviewTime,
                request.getStartDate() == null ? null : request.getStartDate().atStartOfDay());
        wrapper.lt(request.getEndDate() != null, ReviewRecord::getReviewTime,
                request.getEndDate() == null ? null : request.getEndDate().plusDays(1).atStartOfDay());
        wrapper.like(StringUtils.hasText(request.getKeyWord()), ReviewRecord::getQuestionTitleSnapshot,
                request.getKeyWord());
        wrapper.orderByDesc(ReviewRecord::getReviewTime);
        return wrapper;
    }

    /**
     * 汇总正向反馈率和平均主动回忆耗时，供历史页顶部指标展示。
     */
    private void fillHistoryStatistics(ReviewHistoryHomeResp response, Long userId,
                                       String subject, long totalCount) {
        if (totalCount == 0) {
            response.setPositiveFeedbackRate(0);
            response.setCorrectRate(0);
            response.setAverageDurationSeconds(0);
            return;
        }
        long positiveCount = StringUtils.hasText(subject)
                ? reviewRecordMapper.countPositiveFeedbackBySubject(userId, subject)
                : reviewRecordMapper.countPositiveFeedback(userId);
        response.setPositiveFeedbackRate((int) Math.round(positiveCount * 100.0 / totalCount));
        long judgedCount = StringUtils.hasText(subject)
                ? reviewRecordMapper.countJudgedAnswerBySubject(userId, subject)
                : reviewRecordMapper.countJudgedAnswer(userId);
        long correctCount = StringUtils.hasText(subject)
                ? reviewRecordMapper.countCorrectAnswerBySubject(userId, subject)
                : reviewRecordMapper.countCorrectAnswer(userId);
        // 旧记录没有真实作答结果，不进入分母，避免上线后正确率被历史空数据稀释。
        response.setCorrectRate(judgedCount == 0 ? 0
                : (int) Math.round(correctCount * 100.0 / judgedCount));
        double averageDuration = StringUtils.hasText(subject)
                ? reviewRecordMapper.averageAnswerDurationBySubject(userId, subject)
                : reviewRecordMapper.averageAnswerDuration(userId);
        response.setAverageDurationSeconds((int) Math.round(averageDuration));
    }

    /**
     * 构建分页响应
     */
    private ReviewHistoryPageListResp buildPageResponse(ReviewRecord record) {
        // 历史页只返回复习发生时的快照，错题后续编辑不会篡改过去记录。
        ReviewHistoryPageListResp response = new ReviewHistoryPageListResp();
        response.setId(record.getId());
        response.setWrongQuestionId(record.getWrongQuestionId());
        response.setQuestionTitle(record.getQuestionTitleSnapshot());
        response.setSubject(record.getSubject());
        response.setSubjectName(record.getSubjectNameSnapshot());
        response.setFeedback(record.getFeedback());
        response.setFeedbackText(feedbackText(record.getFeedback()));
        response.setStudentAnswer(record.getStudentAnswer());
        response.setIsCorrect(record.getIsCorrect());
        response.setAnswerJudgeType(record.getAnswerJudgeType());
        response.setAnswerDurationSeconds(record.getAnswerDurationSeconds());
        response.setIsOverdue(record.getIsOverdue());
        response.setStageBefore(record.getStageBefore());
        response.setStageAfter(record.getStageAfter());
        response.setMasteryScoreBefore(record.getMasteryScoreBefore());
        response.setMasteryScoreAfter(record.getMasteryScoreAfter());
        response.setMasteryScoreDelta(record.getMasteryScoreDelta());
        response.setCorrectStreakAfter(record.getCorrectStreakAfter());
        response.setWrongStreakAfter(record.getWrongStreakAfter());
        response.setIntervalAfterMinutes(record.getIntervalAfterMinutes());
        response.setReviewTime(record.getReviewTime());
        response.setNextReviewTime(record.getNextReviewTime());
        return response;
    }

    /**
     * 转换反馈文案
     */
    private String feedbackText(Integer feedback) {
        // 数据库历史值可能为空或超出当前枚举，统一用占位符保证列表可渲染。
        if (feedback == null) {
            return "--";
        }
        switch (feedback) {
            case 0:
                return "忘记";
            case 1:
                return "困难";
            case 2:
                return "掌握";
            case 3:
                return "很简单";
            default:
                return "--";
        }
    }
}
