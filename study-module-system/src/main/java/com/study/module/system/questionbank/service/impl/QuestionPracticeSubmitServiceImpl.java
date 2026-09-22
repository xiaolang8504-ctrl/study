package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.dto.request.QuestionPracticeSubmitReq;
import com.study.module.system.questionbank.dto.response.QuestionPracticeSubmitResp;
import com.study.module.system.questionbank.dto.request.QuestionPracticeAnswerViewReq;
import com.study.module.system.questionbank.dto.response.QuestionPracticeAnswerViewResp;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.entity.QuestionRecommendationLog;
import com.study.module.system.questionbank.mapper.QuestionRecommendationLogMapper;
import com.study.module.system.questionbank.service.QuestionPracticeSubmitService;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.study.module.system.questionbank.service.QuestionKnowledgePointService;
import com.study.module.system.questionbank.service.WrongQuestionKnowledgePointService;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * 相似题作答提交服务实现
 */
@Service
public class QuestionPracticeSubmitServiceImpl
        extends ServiceImpl<QuestionRecommendationLogMapper, QuestionRecommendationLog>
        implements QuestionPracticeSubmitService {

    @Autowired
    QuestionBankService questionBankService;

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    QuestionKnowledgePointService questionKnowledgePointService;

    @Autowired
    WrongQuestionKnowledgePointService wrongQuestionKnowledgePointService;

    /**
     * 提交相似题作答结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionPracticeSubmitResp submitQuestionPractice(QuestionPracticeSubmitReq request) {
        Long userId = AccountUtils.getUserId();
        QuestionRecommendationLog log = getOne(new LambdaQueryWrapper<QuestionRecommendationLog>()
                .eq(QuestionRecommendationLog::getId, request.getRecommendationId())
                .eq(QuestionRecommendationLog::getUserId, userId));
        if (log == null) throw new LogicException(ErrorCodeConstants.QUESTION_PRACTICE_NOT_EXIST);
        if (log.getAnswerTime() != null) throw new LogicException(ErrorCodeConstants.QUESTION_PRACTICE_SUBMITTED);
        QuestionBank question = questionBankService.getById(log.getBankQuestionId());
        if (question == null) throw new LogicException(ErrorCodeConstants.QUESTION_BANK_NOT_EXIST);
        boolean objective = "AUTO".equals(question.getJudgeMode());
        if (!objective && log.getAnswerViewedTime() == null) {
            throw new LogicException(ErrorCodeConstants.QUESTION_ANSWER_NOT_VIEWED);
        }
        boolean correct = objective
                ? normalize(request.getStudentAnswer()).equals(normalize(question.getCorrectAnswer()))
                : Boolean.TRUE.equals(request.getSelfCorrect());
        if (objective) log.setStudentAnswer(request.getStudentAnswer());
        log.setIsCorrect(correct ? 1 : 0);
        log.setJudgeType(objective ? "AUTO" : "SELF");
        log.setDurationSeconds(request.getDurationSeconds());
        log.setAnswerTime(LocalDateTime.now());
        updateById(log);
        QuestionPracticeSubmitResp response = new QuestionPracticeSubmitResp();
        response.setCorrect(correct);
        response.setCorrectAnswer(question.getCorrectAnswer());
        response.setAnalysis(question.getAnalysis());
        response.setJudgeType(log.getJudgeType());
        if (!correct) {
            WrongQuestion wrongQuestion = collectWrongQuestion(userId, log, question, request.getStudentAnswer());
            response.setWrongQuestionId(wrongQuestion.getId());
            response.setAutoCollectedWrongQuestion(true);
        } else {
            response.setAutoCollectedWrongQuestion(false);
        }
        return response;
    }

    /**
     * 执行 viewQuestionPracticeAnswer 业务处理。
     */
    @Override
    public QuestionPracticeAnswerViewResp viewQuestionPracticeAnswer(QuestionPracticeAnswerViewReq request) {
        QuestionRecommendationLog log = getOne(new LambdaQueryWrapper<QuestionRecommendationLog>()
                .eq(QuestionRecommendationLog::getId, request.getRecommendationId())
                .eq(QuestionRecommendationLog::getUserId, AccountUtils.getUserId()));
        if (log == null) throw new LogicException(ErrorCodeConstants.QUESTION_PRACTICE_NOT_EXIST);
        if (log.getAnswerTime() != null) throw new LogicException(ErrorCodeConstants.QUESTION_PRACTICE_SUBMITTED);
        QuestionBank question = questionBankService.getById(log.getBankQuestionId());
        if (question == null) throw new LogicException(ErrorCodeConstants.QUESTION_BANK_NOT_EXIST);
        if (!"SELF".equals(question.getJudgeMode())) throw new LogicException(ErrorCodeConstants.ACCESS_DENIED);
        log.setStudentAnswer(request.getStudentAnswer());
        log.setDurationSeconds(request.getDurationSeconds());
        log.setAnswerViewedTime(LocalDateTime.now());
        updateById(log);
        QuestionPracticeAnswerViewResp response = new QuestionPracticeAnswerViewResp();
        response.setCorrectAnswer(question.getCorrectAnswer());
        response.setAnalysis(question.getAnalysis());
        return response;
    }

    /**
     * 规范化答案内容以便判题
     */
    private String normalize(String answer) {
        if (answer == null) return "";
        String value = answer.replaceAll("\\s+", "").replace("，", ",").toUpperCase(Locale.ROOT);
        if (value.matches("[A-D,;、]+")) {
            return value.chars().filter(ch -> ch >= 'A' && ch <= 'D').distinct().sorted()
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
        }
        return value;
    }

    /**
     * 练习答错后自动生成个人错题，并继承题库知识点，后续进入订正和复习链路。
     */
    private WrongQuestion collectWrongQuestion(Long userId, QuestionRecommendationLog log,
                                               QuestionBank question, String studentAnswer) {
        WrongQuestion wrongQuestion = new WrongQuestion();
        wrongQuestion.setGrade(question.getGrade());
        wrongQuestion.setGradeName(question.getGradeName());
        wrongQuestion.setSubject(question.getSubject());
        wrongQuestion.setSubjectName(question.getSubjectName());
        wrongQuestion.setQuestionType(question.getQuestionType());
        wrongQuestion.setQuestionTypeName(question.getQuestionTypeName());
        wrongQuestion.setQuestionTitle(question.getQuestionTitle());
        wrongQuestion.setQuestionContent(question.getQuestionContent());
        wrongQuestion.setWrongAnswer(studentAnswer);
        wrongQuestion.setCorrectAnswer(question.getCorrectAnswer());
        wrongQuestion.setWrongReason("同类练习作答错误，需订正巩固");
        wrongQuestion.setAnalysis(question.getAnalysis());
        wrongQuestion.setLearningPoint(String.join("、", questionKnowledgePointService.pointNames(question.getId())));
        wrongQuestion.setErrorLabels("同类练习错误");
        wrongQuestion.setLevel(question.getDifficulty());
        wrongQuestion.setSource(question.getSource() == null ? "3" : question.getSource());
        wrongQuestion.setSourceName(question.getSourceName() == null ? "同类练习" : question.getSourceName());
        fillQuestionImages(wrongQuestion, question.getImageUrls());
        wrongQuestion.setStatus(WrongQuestionStatus.PENDING_CORRECTION);
        wrongQuestion.setCreateId(userId);
        wrongQuestion.setCreateTime(LocalDateTime.now());
        wrongQuestion.setUpdateTime(LocalDateTime.now());
        if (!wrongQuestionService.save(wrongQuestion)) {
            throw new LogicException(ErrorCodeConstants.CREATE_WRONG_QUESTION_FAIL);
        }
        List<Long> pointIds = questionKnowledgePointService.pointIds(question.getId());
        wrongQuestionKnowledgePointService.rewrite(wrongQuestion.getId(), pointIds);
        return wrongQuestion;
    }

    /**
     * 将题库图片逗号列表尽量映射到个人错题的四个图片字段。
     */
    private void fillQuestionImages(WrongQuestion wrongQuestion, String imageUrls) {
        if (imageUrls == null || imageUrls.trim().isEmpty()) {
            return;
        }
        String[] images = imageUrls.split(",");
        if (images.length > 0) wrongQuestion.setImageUrl(images[0].trim());
        if (images.length > 1) wrongQuestion.setImageUrl2(images[1].trim());
        if (images.length > 2) wrongQuestion.setImageUrl3(images[2].trim());
        if (images.length > 3) wrongQuestion.setImageUrl4(images[3].trim());
    }
}
