package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.module.system.questionbank.entity.KnowledgePoint;
import com.study.module.system.questionbank.entity.KnowledgePointPrerequisite;
import com.study.module.system.questionbank.entity.WrongQuestionKnowledgePoint;
import com.study.module.system.questionbank.mapper.WrongQuestionKnowledgePointMapper;
import com.study.module.system.questionbank.service.KnowledgePointPrerequisiteService;
import com.study.module.system.questionbank.service.KnowledgePointService;
import com.study.module.system.review.dto.response.LearningPathResp;
import com.study.module.system.review.dto.response.LearningPathTaskResp;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.review.service.LearningPathService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.entity.WrongQuestionCorrectionRecord;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.module.system.wrongquestion.service.WrongQuestionCorrectionRecordService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 从学生自己的未掌握错题、知识点映射和间隔复习记录生成路径。
 * 每个节点都返回证据样本数与规则可信度，避免把推断伪装成事实。
 */
@Service
public class LearningPathServiceImpl implements LearningPathService {
    @Autowired private WrongQuestionService wrongQuestionService;
    @Autowired private WrongQuestionKnowledgePointMapper wrongQuestionKnowledgePointMapper;
    @Autowired private KnowledgePointPrerequisiteService knowledgePointPrerequisiteService;
    @Autowired private KnowledgePointService knowledgePointService;
    @Autowired private ReviewItemService reviewItemService;
    @Autowired private WrongQuestionCorrectionRecordService wrongQuestionCorrectionRecordService;

    @Override
    public LearningPathResp learningPath(String subject) {
        Long userId = AccountUtils.getUserId();
        List<WrongQuestion> wrongQuestions = wrongQuestionService.list(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getCreateId, userId).isNull(WrongQuestion::getMergedToId)
                .ne(WrongQuestion::getStatus, 2)
                .eq(StringUtils.hasText(subject), WrongQuestion::getSubject, subject));
        Map<Long, WrongQuestion> questionMap = wrongQuestions.stream()
                .collect(Collectors.toMap(WrongQuestion::getId, item -> item));
        List<WrongQuestionKnowledgePoint> mappings = questionMap.isEmpty() ? new ArrayList<>()
                : wrongQuestionKnowledgePointMapper.selectList(new LambdaQueryWrapper<WrongQuestionKnowledgePoint>()
                .in(WrongQuestionKnowledgePoint::getWrongQuestionId, questionMap.keySet()));
        Set<Long> independentlyCorrectedIds = questionMap.isEmpty() ? new HashSet<>()
                : wrongQuestionCorrectionRecordService.list(new LambdaQueryWrapper<WrongQuestionCorrectionRecord>()
                .in(WrongQuestionCorrectionRecord::getWrongQuestionId, questionMap.keySet())
                .isNotNull(WrongQuestionCorrectionRecord::getThinking))
                .stream().map(WrongQuestionCorrectionRecord::getWrongQuestionId).collect(Collectors.toSet());
        Map<Long, Integer> weakCount = new HashMap<>();
        for (WrongQuestionKnowledgePoint mapping : mappings) {
            WrongQuestion question = questionMap.get(mapping.getWrongQuestionId());
            int evidence = 1 + (independentlyCorrectedIds.contains(mapping.getWrongQuestionId()) ? 1 : 0)
                    + (question != null && (StringUtils.hasText(question.getErrorCauseCodes())
                    || StringUtils.hasText(question.getAbilityLevel())) ? 1 : 0);
            weakCount.merge(mapping.getKnowledgePointId(), evidence, Integer::sum);
        }
        List<Long> pointIds = new ArrayList<>(weakCount.keySet());
        Map<Long, KnowledgePoint> pointMap = pointIds.isEmpty() ? new HashMap<>() : knowledgePointService.listByIds(pointIds)
                .stream().collect(Collectors.toMap(KnowledgePoint::getId, item -> item));
        String selectedSubject = StringUtils.hasText(subject) ? subject : wrongQuestions.stream()
                .map(WrongQuestion::getSubject).filter(StringUtils::hasText).findFirst().orElse(null);
        List<KnowledgePointPrerequisite> relations = knowledgePointPrerequisiteService.list(new LambdaQueryWrapper<KnowledgePointPrerequisite>()
                .eq(KnowledgePointPrerequisite::getEnable, 1));
        Set<Long> prerequisiteIds = relations.stream().filter(item -> weakCount.containsKey(item.getKnowledgePointId()))
                .map(KnowledgePointPrerequisite::getPrerequisitePointId).collect(Collectors.toCollection(HashSet::new));
        if (!prerequisiteIds.isEmpty()) knowledgePointService.listByIds(prerequisiteIds).forEach(point -> pointMap.put(point.getId(), point));
        List<LearningPathTaskResp> tasks = new ArrayList<>();
        prerequisiteIds.stream().filter(pointMap::containsKey).filter(id -> !weakCount.containsKey(id))
                .sorted().forEach(id -> tasks.add(task("PREREQUISITE", pointMap.get(id),
                        "是当前薄弱知识点的前置基础，建议先补缺", 1, 55)));
        weakCount.entrySet().stream().filter(item -> pointMap.containsKey(item.getKey()))
                .sorted(Map.Entry.<Long, Integer>comparingByValue(Comparator.reverseOrder()))
                .forEach(item -> tasks.add(task("CURRENT_WEAKNESS", pointMap.get(item.getKey()),
                        "来自未掌握错题、独立订正、结构化错因和能力层级的聚合", item.getValue(), confidence(item.getValue()))));
        Set<Long> dueQuestionIds = reviewItemService.list(new LambdaQueryWrapper<ReviewItem>()
                .eq(ReviewItem::getUserId, userId).eq(ReviewItem::getItemStatus, 0)
                .le(ReviewItem::getNextReviewTime, LocalDateTime.now()))
                .stream().map(ReviewItem::getWrongQuestionId).collect(Collectors.toSet());
        Set<Long> reinforcementIds = mappings.stream().filter(item -> dueQuestionIds.contains(item.getWrongQuestionId()))
                .map(WrongQuestionKnowledgePoint::getKnowledgePointId).collect(Collectors.toSet());
        reinforcementIds.stream().filter(pointMap::containsKey).sorted().forEach(id -> tasks.add(task("SPACED_REINFORCEMENT",
                pointMap.get(id), "存在到期的间隔复习题，应在补缺后巩固", 1, 70)));
        LearningPathResp response = new LearningPathResp();
        response.setAlgorithmVersion("LEARNING_PATH_RULE_V1"); response.setSubject(selectedSubject); response.setTaskList(tasks);
        return response;
    }

    private LearningPathTaskResp task(String stage, KnowledgePoint point, String reason, int samples, int confidence) {
        LearningPathTaskResp task = new LearningPathTaskResp(); task.setStage(stage); task.setKnowledgePointId(point.getId());
        task.setKnowledgePointName(point.getPointName()); task.setReason(reason); task.setEvidenceSampleCount(samples); task.setConfidence(confidence); return task;
    }

    private int confidence(int samples) { return Math.min(95, 40 + samples * 15); }
}
