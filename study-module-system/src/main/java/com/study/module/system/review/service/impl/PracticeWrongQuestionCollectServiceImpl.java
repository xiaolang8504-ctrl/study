package com.study.module.system.review.service.impl;

import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.review.service.PracticeWrongQuestionCollectService;
import com.study.module.system.review.service.ReviewEnrollmentService;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 专项练习错题沉淀服务实现
 */
@Service
public class PracticeWrongQuestionCollectServiceImpl implements PracticeWrongQuestionCollectService {

    private static final String PRACTICE_SOURCE = "3";

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    ReviewEnrollmentService reviewEnrollmentService;

    /**
     * 执行 collectBankQuestionWrong 业务处理。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long collectBankQuestionWrong(QuestionBank questionBank, Long userId, String studentAnswer) {
        WrongQuestion existing = wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getCreateId, userId)
                .eq(WrongQuestion::getQuestionTitle, defaultString(questionBank.getQuestionTitle()))
                .eq(WrongQuestion::getQuestionContent, defaultString(questionBank.getQuestionContent()))
                .eq(WrongQuestion::getCorrectAnswer, defaultString(questionBank.getCorrectAnswer()))
                .last("LIMIT 1")
                .one();
        if (existing != null) {
            existing.setWrongAnswer(defaultString(studentAnswer));
            existing.setWrongReason("专项练习题库题答错");
            existing.setUpdateTime(LocalDateTime.now());
            boolean reactivated = false;
            if (Integer.valueOf(WrongQuestionStatus.ARCHIVED).equals(existing.getStatus())) {
                existing.setStatus(WrongQuestionStatus.CORRECTED);
                reactivated = true;
            }
            wrongQuestionService.updateById(existing);
            if (reactivated) {
                reviewEnrollmentService.syncWrongQuestionReview(existing);
            }
            return existing.getId();
        }
        LocalDateTime now = LocalDateTime.now();
        WrongQuestion wrongQuestion = new WrongQuestion();
        wrongQuestion.setGrade(questionBank.getGrade());
        wrongQuestion.setGradeName(questionBank.getGradeName());
        wrongQuestion.setSubject(questionBank.getSubject());
        wrongQuestion.setSubjectName(questionBank.getSubjectName());
        wrongQuestion.setQuestionType(questionBank.getQuestionType());
        wrongQuestion.setQuestionTypeName(questionBank.getQuestionTypeName());
        wrongQuestion.setQuestionTitle(defaultString(questionBank.getQuestionTitle()));
        wrongQuestion.setQuestionContent(defaultString(questionBank.getQuestionContent()));
        wrongQuestion.setWrongAnswer(defaultString(studentAnswer));
        wrongQuestion.setCorrectAnswer(defaultString(questionBank.getCorrectAnswer()));
        wrongQuestion.setWrongReason("专项练习题库题答错");
        wrongQuestion.setAnalysis(questionBank.getAnalysis());
        fillImages(wrongQuestion, questionBank.getImageUrls());
        wrongQuestion.setLearningPoint("");
        wrongQuestion.setErrorLabels("专项练习错误");
        wrongQuestion.setLevel(questionBank.getDifficulty());
        wrongQuestion.setSource(PRACTICE_SOURCE);
        wrongQuestion.setSourceName("日常练习");
        wrongQuestion.setStatus(WrongQuestionStatus.CORRECTED);
        wrongQuestion.setCreateId(userId);
        wrongQuestion.setCreateTime(now);
        wrongQuestion.setUpdateTime(now);
        wrongQuestionService.save(wrongQuestion);
        reviewEnrollmentService.syncWrongQuestionReview(wrongQuestion);
        return wrongQuestion.getId();
    }

    /**
     * 更新业务数据。
     */
    private void fillImages(WrongQuestion wrongQuestion, String imageUrls) {
        if (!StringUtils.hasText(imageUrls)) {
            return;
        }
        String[] images = imageUrls.split(",");
        if (images.length > 0) {
            wrongQuestion.setImageUrl(images[0].trim());
        }
        if (images.length > 1) {
            wrongQuestion.setImageUrl2(images[1].trim());
        }
        if (images.length > 2) {
            wrongQuestion.setImageUrl3(images[2].trim());
        }
        if (images.length > 3) {
            wrongQuestion.setImageUrl4(images[3].trim());
        }
    }

    /**
     * 标准化并计算业务数据。
     */
    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
