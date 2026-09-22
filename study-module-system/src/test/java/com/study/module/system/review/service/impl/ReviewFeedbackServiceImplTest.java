package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.study.module.system.review.constants.ReviewCacheKey;
import com.study.module.system.review.constants.ReviewFeedback;
import com.study.module.system.review.constants.ReviewItemStatus;
import com.study.module.system.review.dto.request.SubmitReviewFeedbackReq;
import com.study.module.system.review.dto.response.SubmitReviewFeedbackResp;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.mapper.ReviewItemMapper;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.review.service.ReviewRecordService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.module.system.wrongquestion.service.WrongQuestionTimelineService;
import com.yunshang.budget.common.redis.RedisService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ReviewFeedbackServiceImplTest {

    @Mock private ReviewItemService reviewItemService;
    @Mock private ReviewItemMapper reviewItemMapper;
    @Mock private ReviewRecordService reviewRecordService;
    @Mock private WrongQuestionService wrongQuestionService;
    @Mock private WrongQuestionTimelineService timelineService;
    @Mock private RedisService redisService;
    @Mock(answer = Answers.RETURNS_SELF) private LambdaQueryChainWrapper<ReviewRecord> recordQuery;
    @Mock(answer = Answers.RETURNS_SELF) private LambdaQueryChainWrapper<WrongQuestion> wrongQuestionQuery;
    @InjectMocks private ReviewFeedbackServiceImpl service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void correctAnswerWithoutServerDraftDoesNotAdvanceMastery() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(7L, null, Collections.emptyList()));
        LocalDateTime revealTime = LocalDateTime.now().minusMinutes(2);
        SubmitReviewFeedbackReq request = new SubmitReviewFeedbackReq();
        request.setRequestId("attempt-1");
        request.setReviewItemId(11L);
        request.setRevealToken("ticket");
        request.setStartTime(revealTime.minusMinutes(3));
        request.setStudentAnswer("A");
        request.setFeedback(ReviewFeedback.EASY);

        ReviewItem item = new ReviewItem();
        item.setId(11L);
        item.setWrongQuestionId(21L);
        item.setItemStatus(ReviewItemStatus.NORMAL);
        item.setStage(5);
        item.setMasteryScore(95);
        item.setCorrectStreak(2);
        item.setVersion(0);
        item.setNextReviewTime(revealTime.minusDays(1));
        WrongQuestion question = new WrongQuestion();
        question.setId(21L);
        question.setStatus(1);
        question.setQuestionTypeName("单选题");
        question.setCorrectAnswer("A");

        when(reviewRecordService.lambdaQuery()).thenReturn(recordQuery);
        doReturn(null).when(recordQuery).one();
        when(reviewItemService.checkReviewItem(11L, 7L)).thenReturn(item);
        when(redisService.get(ReviewCacheKey.answerRevealKey(7L, 11L)))
                .thenReturn("ticket:0:" + revealTime);
        when(wrongQuestionService.lambdaQuery()).thenReturn(wrongQuestionQuery);
        doReturn(question).when(wrongQuestionQuery).one();
        when(reviewItemMapper.updateReviewFeedback(any(ReviewItem.class), anyInt())).thenReturn(1);
        when(reviewRecordService.save(any(ReviewRecord.class))).thenReturn(true);

        SubmitReviewFeedbackResp response = service.submitReviewFeedback(request);

        ArgumentCaptor<ReviewRecord> savedRecord = ArgumentCaptor.forClass(ReviewRecord.class);
        verify(reviewRecordService).save(savedRecord.capture());
        assertEquals(0, savedRecord.getValue().getIsIndependent());
        assertEquals(95, savedRecord.getValue().getMasteryScoreAfter());
        assertEquals(0, savedRecord.getValue().getCorrectStreakAfter());
        assertFalse(response.getMastered());
    }
}
