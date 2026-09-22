package com.study.module.system.review.service.impl;

import com.study.module.system.review.constants.ReviewItemStatus;
import com.study.module.system.review.constants.ReviewCacheKey;
import com.study.module.system.review.dto.response.ReviewAnswerResp;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.service.ReviewAnswerService;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.module.system.wrongquestion.service.WrongQuestionTimelineService;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.security.utils.AccountUtils;
import com.yunshang.budget.common.redis.RedisService;
import cn.hutool.crypto.SecureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 查看复习答案服务实现
 */
@Service
public class ReviewAnswerServiceImpl implements ReviewAnswerService {

    @Autowired
    ReviewItemService reviewItemService;

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    WrongQuestionTimelineService wrongQuestionTimelineService;

    @Autowired
    RedisService redisService;

    @Override
    public void saveReviewAnswerDraft(Long reviewItemId, String studentAnswer) {
        Long userId = AccountUtils.getUserId();
        ReviewItem reviewItem = reviewItemService.checkReviewItem(reviewItemId, userId);
        if (!Integer.valueOf(ReviewItemStatus.NORMAL).equals(reviewItem.getItemStatus())) {
            throw new LogicException(ErrorCodeConstants.REVIEW_ITEM_NOT_AVAILABLE);
        }
        String value = answerDigest(studentAnswer) + ":" + LocalDateTime.now();
        Boolean saved = redisService.set(ReviewCacheKey.answerDraftKey(userId, reviewItemId), value,
                ReviewCacheKey.ANSWER_REVEAL_TTL_SECONDS);
        if (!Boolean.TRUE.equals(saved)) {
            throw new LogicException(ErrorCodeConstants.REVIEW_ANSWER_TICKET_FAIL);
        }
    }

    /**
     * 提交复习答案
     */
    @Override
    public ReviewAnswerResp reviewAnswer(Long reviewItemId) {
        Long userId = AccountUtils.getUserId();

        // 查看答案前校验任务状态和数据归属，避免越权读取其他学生的错题答案。
        ReviewItem reviewItem = reviewItemService.checkReviewItem(reviewItemId, userId);
        if (!Integer.valueOf(ReviewItemStatus.NORMAL).equals(reviewItem.getItemStatus())) {
            throw new LogicException(ErrorCodeConstants.REVIEW_ITEM_NOT_AVAILABLE);
        }
        WrongQuestion wrongQuestion = wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getId, reviewItem.getWrongQuestionId())
                .eq(WrongQuestion::getCreateId, userId)
                .one();
        if (wrongQuestion == null) {
            throw new LogicException(ErrorCodeConstants.REVIEW_ITEM_NOT_EXIST);
        }

        // revealTime 与一次性凭证由服务端生成，反馈接口据此验证学生确实执行过“查看答案”。
        LocalDateTime revealTime = LocalDateTime.now();
        String revealToken = UUID.randomUUID().toString().replace("-", "");
        String ticketValue = revealToken + ":" + defaultVersion(reviewItem.getVersion()) + ":" + revealTime;
        Boolean cached = redisService.set(ReviewCacheKey.answerRevealKey(userId, reviewItemId),
                ticketValue, ReviewCacheKey.ANSWER_REVEAL_TTL_SECONDS);
        if (!Boolean.TRUE.equals(cached)) {
            throw new LogicException(ErrorCodeConstants.REVIEW_ANSWER_TICKET_FAIL);
        }
        wrongQuestionTimelineService.record(wrongQuestion.getId(), "ANSWER_REVEALED", "STUDENT",
                "学生已查看参考答案与解析，本次后续反馈不计为独立作答", userId);
        ReviewAnswerResp response = new ReviewAnswerResp();
        response.setReviewItemId(reviewItemId);
        response.setWrongAnswer(wrongQuestion.getWrongAnswer());
        response.setCorrectAnswer(wrongQuestion.getCorrectAnswer());
        response.setAnalysis(wrongQuestion.getAnalysis());
        response.setRevealTime(revealTime);
        response.setRevealToken(revealToken);
        return response;
    }

    /**
     * 获取默认版本
     */
    private int defaultVersion(Integer version) {
        return version == null ? 0 : version;
    }

    private String answerDigest(String answer) {
        String normalized = answer == null ? "" : answer.replaceAll("<[^>]+>", "")
                .replaceAll("[\\s，,。；;：:、]", "").toLowerCase().trim();
        return SecureUtil.sha256(normalized);
    }
}
