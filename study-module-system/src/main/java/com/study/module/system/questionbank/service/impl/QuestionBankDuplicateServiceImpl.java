package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.dto.request.QuestionBankDuplicateCheckReq;
import com.study.module.system.questionbank.dto.request.QuestionBankDuplicateCleanReq;
import com.study.module.system.questionbank.dto.request.QuestionBankDuplicateHistoryReq;
import com.study.module.system.questionbank.dto.response.QuestionBankDuplicatePairResp;
import com.study.module.system.questionbank.dto.response.QuestionBankDuplicateResp;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.entity.QuestionBankImage;
import com.study.module.system.questionbank.entity.QuestionKnowledgePoint;
import com.study.module.system.questionbank.entity.QuestionPracticeAppeal;
import com.study.module.system.questionbank.entity.QuestionRecommendationLog;
import com.study.module.system.questionbank.entity.QuestionReport;
import com.study.module.system.questionbank.mapper.QuestionBankMapper;
import com.study.module.system.questionbank.service.QuestionBankDeleteService;
import com.study.module.system.questionbank.service.QuestionBankDuplicateService;
import com.study.module.system.questionbank.service.QuestionBankImageService;
import com.study.module.system.questionbank.service.QuestionKnowledgePointService;
import com.study.module.system.questionbank.service.QuestionPracticeAppealService;
import com.study.module.system.questionbank.service.QuestionRecommendationLogService;
import com.study.module.system.questionbank.service.QuestionReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于规范化字符二元组 Dice 系数的重复题检测实现
 */
@Service
public class QuestionBankDuplicateServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank>
        implements QuestionBankDuplicateService {

    private static final int DEFAULT_SIMILARITY = 82;

    @Autowired
    private QuestionBankDeleteService questionBankDeleteService;

    @Autowired
    private QuestionKnowledgePointService questionKnowledgePointService;

    @Autowired
    private QuestionBankImageService questionBankImageService;

    @Autowired
    private QuestionRecommendationLogService questionRecommendationLogService;

    @Autowired
    private QuestionReportService questionReportService;

    @Autowired
    private QuestionPracticeAppealService questionPracticeAppealService;

    /**
     * 执行 duplicateQuestionList 业务处理。
     */
    @Override
    public List<QuestionBankDuplicateResp> duplicateQuestionList(QuestionBankDuplicateCheckReq request) {
        List<QuestionBank> candidates = list(new LambdaQueryWrapper<QuestionBank>()
                .eq(QuestionBank::getGrade, request.getGrade())
                .eq(QuestionBank::getSubject, request.getSubject())
                .ne(request.getId() != null, QuestionBank::getId, request.getId()));
        String source = normalize(request.getQuestionContent());
        return candidates.stream().map(item -> toDuplicate(item, similarity(source, normalize(item.getQuestionContent()))))
                .filter(item -> item.getSimilarity() >= DEFAULT_SIMILARITY)
                .sorted((left, right) -> Integer.compare(right.getSimilarity(), left.getSimilarity()))
                .limit(20)
                .collect(Collectors.toList());
    }

    /**
     * 查询相关业务数据历史记录。
     */
    @Override
    public List<QuestionBankDuplicatePairResp> duplicateQuestionHistory(QuestionBankDuplicateHistoryReq request) {
        int threshold = request.getSimilarityThreshold() == null ? DEFAULT_SIMILARITY : request.getSimilarityThreshold();
        int limit = request.getLimit() == null ? 100 : request.getLimit();
        LambdaQueryWrapper<QuestionBank> wrapper = new LambdaQueryWrapper<QuestionBank>()
                .eq(StringUtils.hasText(request.getGrade()), QuestionBank::getGrade, request.getGrade())
                .eq(StringUtils.hasText(request.getSubject()), QuestionBank::getSubject, request.getSubject())
                .orderByAsc(QuestionBank::getGrade, QuestionBank::getSubject, QuestionBank::getId);
        List<QuestionBank> questions = list(wrapper);
        List<QuestionBankDuplicatePairResp> result = new ArrayList<>();
        for (int i = 0; i < questions.size() && result.size() < limit; i++) {
            QuestionBank first = questions.get(i);
            String source = normalize(first.getQuestionContent());
            for (int j = i + 1; j < questions.size() && result.size() < limit; j++) {
                QuestionBank second = questions.get(j);
                if (!first.getGrade().equals(second.getGrade()) || !first.getSubject().equals(second.getSubject())) {
                    continue;
                }
                int score = similarity(source, normalize(second.getQuestionContent()));
                if (score >= threshold) {
                    QuestionBankDuplicatePairResp pair = new QuestionBankDuplicatePairResp();
                    pair.setFirstQuestion(toDuplicate(first, score));
                    pair.setSecondQuestion(toDuplicate(second, score));
                    pair.setSimilarity(score);
                    pair.setMatchType(score == 100 ? "EXACT" : "HIGH_SIMILAR");
                    result.add(pair);
                }
            }
        }
        return result;
    }

    /**
     * 清理相关业务数据。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanDuplicateQuestion(QuestionBankDuplicateCleanReq request) {
        QuestionBank keep = getById(request.getKeepId());
        QuestionBank deleted = getById(request.getDeleteId());
        if (request.getKeepId().equals(request.getDeleteId()) || keep == null || deleted == null
                || !keep.getGrade().equals(deleted.getGrade())
                || !keep.getSubject().equals(deleted.getSubject())
                || similarity(normalize(keep.getQuestionContent()), normalize(deleted.getQuestionContent()))
                < DEFAULT_SIMILARITY) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_NOT_EXIST);
        }
        questionBankDeleteService.deleteQuestionBank(request.getDeleteId());
    }

    /**
     * 合并相关业务数据。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mergeDuplicateQuestion(QuestionBankDuplicateCleanReq request) {
        validateDuplicatePair(request);
        List<Long> keepPointIds = questionKnowledgePointService.pointIds(request.getKeepId());
        List<QuestionKnowledgePoint> deletedPoints = questionKnowledgePointService.list(
                new LambdaQueryWrapper<QuestionKnowledgePoint>().eq(QuestionKnowledgePoint::getQuestionId, request.getDeleteId()));
        for (QuestionKnowledgePoint point : deletedPoints) {
            if (!keepPointIds.contains(point.getKnowledgePointId())) {
                point.setQuestionId(request.getKeepId());
                questionKnowledgePointService.updateById(point);
            }
        }
        questionKnowledgePointService.remove(new LambdaQueryWrapper<QuestionKnowledgePoint>()
                .eq(QuestionKnowledgePoint::getQuestionId, request.getDeleteId()));
        questionBankImageService.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<QuestionBankImage>()
                .eq(QuestionBankImage::getQuestionId, request.getDeleteId())
                .set(QuestionBankImage::getQuestionId, request.getKeepId()));
        questionRecommendationLogService.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<QuestionRecommendationLog>()
                .eq(QuestionRecommendationLog::getBankQuestionId, request.getDeleteId())
                .set(QuestionRecommendationLog::getBankQuestionId, request.getKeepId()));
        questionReportService.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<QuestionReport>()
                .eq(QuestionReport::getBankQuestionId, request.getDeleteId())
                .set(QuestionReport::getBankQuestionId, request.getKeepId()));
        questionPracticeAppealService.update(new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<QuestionPracticeAppeal>()
                .eq(QuestionPracticeAppeal::getBankQuestionId, request.getDeleteId())
                .set(QuestionPracticeAppeal::getBankQuestionId, request.getKeepId()));
        questionBankDeleteService.deleteQuestionBank(request.getDeleteId());
    }

    /**
     * 校验业务数据。
     */
    private void validateDuplicatePair(QuestionBankDuplicateCleanReq request) {
        QuestionBank keep = getById(request.getKeepId());
        QuestionBank deleted = getById(request.getDeleteId());
        if (request.getKeepId().equals(request.getDeleteId()) || keep == null || deleted == null
                || !keep.getGrade().equals(deleted.getGrade()) || !keep.getSubject().equals(deleted.getSubject())
                || similarity(normalize(keep.getQuestionContent()), normalize(deleted.getQuestionContent())) < DEFAULT_SIMILARITY) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_NOT_EXIST);
        }
    }

    /**
     * 转换业务数据。
     */
    private QuestionBankDuplicateResp toDuplicate(QuestionBank question, int score) {
        QuestionBankDuplicateResp response = new QuestionBankDuplicateResp();
        response.setId(question.getId());
        response.setQuestionTitle(question.getQuestionTitle());
        response.setQuestionContent(question.getQuestionContent());
        response.setSimilarity(score);
        response.setMatchType(score == 100 ? "EXACT" : "HIGH_SIMILAR");
        response.setCreateTime(question.getCreateTime());
        return response;
    }

    /**
     * 标准化并计算业务数据。
     */
    private String normalize(String content) {
        if (content == null) {
            return "";
        }
        return content.toLowerCase(Locale.ROOT)
                .replaceAll("<[^>]+>", "")
                .replaceAll("[\\p{P}\\p{S}\\s]+", "");
    }

    /**
     * 标准化并计算业务数据。
     */
    private int similarity(String left, String right) {
        if (left.equals(right)) {
            return 100;
        }
        if (left.length() < 2 || right.length() < 2) {
            return 0;
        }
        Map<String, Integer> leftPairs = pairs(left);
        Map<String, Integer> rightPairs = pairs(right);
        int intersection = 0;
        int leftCount = 0;
        int rightCount = 0;
        for (Integer count : leftPairs.values()) {
            leftCount += count;
        }
        for (Map.Entry<String, Integer> entry : rightPairs.entrySet()) {
            rightCount += entry.getValue();
            intersection += Math.min(entry.getValue(), leftPairs.getOrDefault(entry.getKey(), 0));
        }
        return (int) Math.round(200D * intersection / (leftCount + rightCount));
    }

    /**
     * 执行 pairs 辅助处理。
     */
    private Map<String, Integer> pairs(String content) {
        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < content.length() - 1; i++) {
            String pair = content.substring(i, i + 2);
            result.put(pair, result.getOrDefault(pair, 0) + 1);
        }
        return result;
    }
}
