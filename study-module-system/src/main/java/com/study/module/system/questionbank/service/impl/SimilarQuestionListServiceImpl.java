package com.study.module.system.questionbank.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.questionbank.dto.request.SimilarQuestionListReq;
import com.study.module.system.questionbank.dto.response.*;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.entity.QuestionRecommendationLog;
import com.study.module.system.questionbank.entity.QuestionExperiment;
import com.study.module.system.questionbank.mapper.QuestionRecommendationLogMapper;
import com.study.module.system.questionbank.service.*;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class SimilarQuestionListServiceImpl
        extends ServiceImpl<QuestionRecommendationLogMapper, QuestionRecommendationLog>
        implements SimilarQuestionListService {

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    QuestionBankService questionBankService;

    @Autowired
    QuestionKnowledgePointService questionKnowledgePointService;

    @Autowired
    WrongQuestionKnowledgePointService wrongQuestionKnowledgePointService;

    @Autowired
    QuestionBankImageService questionBankImageService;

    @Autowired
    QuestionExperimentService questionExperimentService;

    /**
     * 查询推荐的相似题列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SimilarQuestionListResp> similarQuestionList(SimilarQuestionListReq request) {
        Long userId = AccountUtils.getUserId();
        WrongQuestion wrong = wrongQuestionService.checkWrongQuestion(request.getWrongQuestionId());
        int limit = request.getLimit() == null ? 5 : request.getLimit();
        int difficulty = wrong.getLevel() == null ? 3 : wrong.getLevel();
        String keyword = StringUtils.hasText(wrong.getLearningPoint()) ? wrong.getLearningPoint()
                : (StringUtils.hasText(wrong.getQuestionTitle()) ? wrong.getQuestionTitle() : wrong.getQuestionContent());
        List<Long> pointIds = wrongQuestionKnowledgePointService.resolvePointIds(wrong);
        LinkedHashMap<Long, QuestionBank> candidates = new LinkedHashMap<>();
        Map<Long, String> matchTypes = new HashMap<>();
        // 使用稳定的用户分组切换知识点和全文检索顺序，便于对比两种召回策略的效果。
        QuestionExperiment experiment = questionExperimentService.currentRunningExperiment();
        int traffic = experiment == null ? 100 : experiment.getGroupATraffic();
        String group = Math.floorMod(Long.hashCode(userId), 100) < traffic ? "A" : "B";
        if (!pointIds.isEmpty() && "A".equals(group)) add(candidates, matchTypes,
                questionBankService.selectKnowledgeCandidates(userId, wrong.getGrade(), wrong.getSubject(),
                        wrong.getQuestionType(), difficulty, pointIds, limit * 3), "KNOWLEDGE", limit);
        if (candidates.size() < limit && StringUtils.hasText(keyword)) add(candidates, matchTypes,
                questionBankService.selectSimilarCandidates(userId, wrong.getGrade(), wrong.getSubject(),
                        wrong.getQuestionType(), difficulty, keyword, limit * 3), "FULLTEXT", limit);
        if (!pointIds.isEmpty() && "B".equals(group) && candidates.size() < limit) add(candidates, matchTypes,
                questionBankService.selectKnowledgeCandidates(userId, wrong.getGrade(), wrong.getSubject(),
                        wrong.getQuestionType(), difficulty, pointIds, limit * 3), "KNOWLEDGE", limit);
        // 候选不足时逐级放宽题型和近期曝光限制，保证题库较小时仍能返回练习题。
        if (candidates.size() < limit) add(candidates, matchTypes, questionBankService.selectFallbackCandidates(userId,
                wrong.getGrade(), wrong.getSubject(), wrong.getQuestionType(), difficulty, true, true, limit * 2), "STRUCTURED", limit);
        if (candidates.size() < limit) add(candidates, matchTypes, questionBankService.selectFallbackCandidates(userId,
                wrong.getGrade(), wrong.getSubject(), wrong.getQuestionType(), difficulty, false, true, limit * 2), "RELAXED_TYPE", limit);
        if (candidates.size() < Math.min(3, limit)) add(candidates, matchTypes, questionBankService.selectFallbackCandidates(userId,
                wrong.getGrade(), wrong.getSubject(), wrong.getQuestionType(), difficulty, false, false, limit * 2), "REUSED_FALLBACK", limit);
        String batchNo = UUID.randomUUID().toString().replace("-", "");
        List<SimilarQuestionListResp> result = new ArrayList<>();
        int rank = 0;
        for (QuestionBank question : candidates.values()) {
            String matchType = matchTypes.get(question.getId());
            List<Long> questionPoints = questionKnowledgePointService.pointIds(question.getId());
            long overlap = questionPoints.stream().filter(pointIds::contains).count();
            double score = score(matchType, overlap, Objects.equals(question.getQuestionType(), wrong.getQuestionType()),
                    Math.abs(question.getDifficulty() - difficulty), rank);
            String reason = reason(matchType, overlap, questionKnowledgePointService.pointNames(question.getId()),
                    question.getDifficulty(), difficulty);
            // 推荐记录与响应在同一事务内生成，确保返回的 recommendationId 可用于后续反馈追踪。
            QuestionRecommendationLog log = new QuestionRecommendationLog();
            log.setBatchNo(batchNo); log.setUserId(userId); log.setWrongQuestionId(wrong.getId());
            log.setBankQuestionId(question.getId()); log.setRankNo(++rank); log.setRecommendScore(score);
            log.setAlgorithmVersion("SIMILAR_V2"); log.setMatchType(matchType); log.setRecommendReason(reason);
            log.setExperimentGroup(group); log.setExperimentId(experiment == null ? null : experiment.getId());
            log.setExposureTime(LocalDateTime.now()); log.setCreateTime(LocalDateTime.now());
            save(log);
            SimilarQuestionListResp response = new SimilarQuestionListResp();
            response.setRecommendationId(log.getId()); response.setQuestionId(question.getId());
            response.setQuestionTitle(question.getQuestionTitle()); response.setQuestionContent(question.getQuestionContent());
            response.setContentFormat(question.getContentFormat()); response.setImageUrls(question.getImageUrls());
            response.setOptionsJson(question.getOptionsJson()); response.setQuestionType(question.getQuestionType());
            response.setJudgeMode(question.getJudgeMode());
            response.setDifficulty(question.getDifficulty()); response.setRecommendScore(score); response.setMatchType(matchType);
            response.setRecommendReason(reason); response.setExperimentGroup(group);
            response.setImages(questionBankImageService.imageList(question.getId()));
            response.setKnowledgePointNames(questionKnowledgePointService.pointNames(question.getId()));
            result.add(response);
        }
        return result;
    }

    /**
     * 将候选题加入推荐结果并记录匹配类型
     */
    private void add(Map<Long, QuestionBank> target, Map<Long, String> types, List<QuestionBank> source, String type, int limit) {
        for (QuestionBank q : source) if (target.size() < limit && !target.containsKey(q.getId())) { target.put(q.getId(), q); types.put(q.getId(), type); }
    }
    /**
     * 计算题目推荐分数
     */
    private double score(String type, long overlap, boolean sameType, int gap, int rank) {
        double base = "KNOWLEDGE".equals(type) ? 65 : ("FULLTEXT".equals(type) ? 55 : 42);
        return Math.max(20D, Math.min(99D, base + Math.min(20, overlap * 10) + (sameType ? 8 : 0) - gap * 5D - rank * 2D));
    }
    /**
     * 生成面向学生的推荐原因
     */
    private String reason(String type, long overlap, List<String> names, int difficulty, int target) {
        if ("KNOWLEDGE".equals(type)) return "命中" + overlap + "个知识点：" + String.join("、", names);
        if ("FULLTEXT".equals(type)) return "题干语义和题型相近";
        if ("REUSED_FALLBACK".equals(type)) return "题库数量不足，复用最匹配的历史推荐题";
        return "同年级同科目，难度差" + Math.abs(difficulty - target) + "级";
    }
}
