package com.study.module.system.review.service.impl;

import com.study.module.system.review.dto.request.ReviewWeakPointPracticeReq;
import com.study.module.system.review.dto.response.ReviewPracticeQuestionResp;
import com.study.module.system.review.dto.response.ReviewWeakPointPracticeResp;
import com.study.module.system.review.service.ReviewWeakPointPracticeService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 薄弱知识点专项练习服务实现
 */
@Service
public class ReviewWeakPointPracticeServiceImpl implements ReviewWeakPointPracticeService {

    @Autowired
    WrongQuestionService wrongQuestionService;

    /**
     * 生成薄弱点练习
     */
    @Override
    public ReviewWeakPointPracticeResp generateWeakPointPractice(ReviewWeakPointPracticeReq request) {
        Long userId = AccountUtils.getUserId();
        String learningPoint = request.getLearningPoint().trim();

        // 仅从当前学生本人的已订正/已掌握错题中组卷，未掌握题优先于已掌握题。
        List<WrongQuestion> questions = wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getCreateId, userId)
                .eq(WrongQuestion::getLearningPoint, learningPoint)
                .in(WrongQuestion::getStatus, 1, 2)
                .orderByAsc(WrongQuestion::getStatus)
                .orderByDesc(WrongQuestion::getLevel)
                .orderByDesc(WrongQuestion::getUpdateTime)
                .last("LIMIT " + request.getQuestionCount())
                .list();

        ReviewWeakPointPracticeResp response = new ReviewWeakPointPracticeResp();
        response.setLearningPoint(learningPoint);
        response.setQuestionCount(questions.size());
        response.setRecommendation(buildRecommendation(learningPoint, questions));
        response.setQuestionList(questions.stream().map(this::buildQuestionResponse)
                .collect(Collectors.toList()));
        return response;
    }

    /**
     * 构建题目响应
     */
    private ReviewPracticeQuestionResp buildQuestionResponse(WrongQuestion question) {
        // 专项练习阶段不返回正确答案和解析，保证学生先独立作答。
        ReviewPracticeQuestionResp response = new ReviewPracticeQuestionResp();
        response.setWrongQuestionId(question.getId());
        response.setQuestionTitle(question.getQuestionTitle());
        response.setQuestionContent(question.getQuestionContent());
        response.setSubjectName(question.getSubjectName());
        response.setQuestionTypeName(question.getQuestionTypeName());
        response.setLearningPoint(question.getLearningPoint());
        response.setLevel(question.getLevel());
        response.setStatus(question.getStatus());
        response.setImageUrl(question.getImageUrl());
        response.setImageUrl2(question.getImageUrl2());
        response.setImageUrl3(question.getImageUrl3());
        response.setImageUrl4(question.getImageUrl4());
        return response;
    }

    /**
     * 构建推荐结果
     */
    private String buildRecommendation(String learningPoint, List<WrongQuestion> questions) {
        if (questions.isEmpty()) {
            return "暂未找到“" + learningPoint + "”的可练习错题，请先补充该知识点的错题。";
        }
        long unmasteredCount = questions.stream()
                .filter(question -> !Integer.valueOf(2).equals(question.getStatus()))
                .count();
        return "已优先选择" + unmasteredCount + "道未掌握题，并按难度和最近错误时间排序。";
    }
}
