package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.convert.QuestionBankConvert;
import com.study.module.system.questionbank.dto.request.QuestionPracticeAppealCreateReq;
import com.study.module.system.questionbank.dto.request.QuestionPracticeAppealReviewReq;
import com.study.module.system.questionbank.dto.response.QuestionPracticeAppealListResp;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.entity.QuestionPracticeAppeal;
import com.study.module.system.questionbank.entity.QuestionRecommendationLog;
import com.study.module.system.questionbank.mapper.QuestionPracticeAppealMapper;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.study.module.system.questionbank.service.QuestionPracticeAppealService;
import com.study.module.system.questionbank.service.QuestionRecommendationLogService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 主观题作答申诉服务实现。
 */
@Service
public class QuestionPracticeAppealServiceImpl extends ServiceImpl<QuestionPracticeAppealMapper, QuestionPracticeAppeal>
        implements QuestionPracticeAppealService {

    @Autowired
    private QuestionRecommendationLogService questionRecommendationLogService;

    @Autowired
    private QuestionBankService questionBankService;

    /**
     * 创建主观题作答申诉。
     */
    @Override
    public void createQuestionPracticeAppeal(QuestionPracticeAppealCreateReq request) {
        Long userId = AccountUtils.getUserId();
        QuestionRecommendationLog log = questionRecommendationLogService.getOne(
                new LambdaQueryWrapper<QuestionRecommendationLog>().eq(QuestionRecommendationLog::getId, request.getRecommendationId())
                        .eq(QuestionRecommendationLog::getUserId, userId));
        if (log == null || log.getAnswerTime() == null || !"SELF".equals(log.getJudgeType())) {
            throw new LogicException(ErrorCodeConstants.QUESTION_PRACTICE_NOT_EXIST);
        }
        if (count(new LambdaQueryWrapper<QuestionPracticeAppeal>()
                .eq(QuestionPracticeAppeal::getRecommendationId, log.getId())) > 0) {
            throw new LogicException(ErrorCodeConstants.QUESTION_PRACTICE_APPEAL_EXIST);
        }
        QuestionPracticeAppeal entity = new QuestionPracticeAppeal();
        entity.setRecommendationId(log.getId());
        entity.setUserId(userId);
        entity.setBankQuestionId(log.getBankQuestionId());
        entity.setAppealReason(request.getAppealReason());
        entity.setStatus(0);
        entity.setCreateTime(LocalDateTime.now());
        save(entity);
    }

    /**
     * 查询主观题作答申诉列表。
     */
    @Override
    public List<QuestionPracticeAppealListResp> questionPracticeAppealList(Integer status) {
        return list(new LambdaQueryWrapper<QuestionPracticeAppeal>().eq(status != null, QuestionPracticeAppeal::getStatus, status)
                .orderByDesc(QuestionPracticeAppeal::getId)).stream().map(item -> {
            QuestionPracticeAppealListResp response = QuestionBankConvert.INSTANCE.toQuestionPracticeAppealListResp(item);
            QuestionRecommendationLog log = questionRecommendationLogService.getById(item.getRecommendationId());
            QuestionBank question = questionBankService.getById(item.getBankQuestionId());
            if (log != null) {
                response.setStudentAnswer(log.getStudentAnswer());
                response.setSelfCorrect(log.getIsCorrect());
            }
            if (question != null) {
                response.setQuestionTitle(question.getQuestionTitle());
                response.setCorrectAnswer(question.getCorrectAnswer());
            }
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 审核主观题作答申诉。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reviewQuestionPracticeAppeal(QuestionPracticeAppealReviewReq request) {
        QuestionPracticeAppeal appeal = getById(request.getId());
        if (appeal == null || appeal.getStatus() != 0) {
            throw new LogicException(ErrorCodeConstants.QUESTION_PRACTICE_APPEAL_NOT_EXIST);
        }
        QuestionRecommendationLog log = questionRecommendationLogService.getById(appeal.getRecommendationId());
        if (log == null) {
            throw new LogicException(ErrorCodeConstants.QUESTION_PRACTICE_NOT_EXIST);
        }
        log.setIsCorrect(Boolean.TRUE.equals(request.getCorrect()) ? 1 : 0);
        log.setJudgeType("TEACHER_REVIEW");
        questionRecommendationLogService.updateById(log);
        appeal.setStatus(1);
        appeal.setReviewCorrect(log.getIsCorrect());
        appeal.setReviewRemark(request.getReviewRemark());
        appeal.setReviewerId(AccountUtils.getUserId());
        appeal.setReviewTime(LocalDateTime.now());
        updateById(appeal);
    }
}
