package com.study.module.system.review.service.impl;

import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.review.constants.PracticeSessionStatus;
import com.study.module.system.review.constants.PracticeQuestionSource;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.study.module.system.review.dto.request.PracticeSessionCreateReq;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import com.study.module.system.review.dto.response.PracticeSessionPreviewResp;
import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.service.PracticeSessionCreateService;
import com.study.module.system.review.service.PracticeSessionQuestionService;
import com.study.module.system.review.service.PracticeSessionService;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 创建专项练习服务实现
 */
@Service
public class PracticeSessionCreateServiceImpl implements PracticeSessionCreateService {

    @Autowired
    PracticeSessionService practiceSessionService;

    @Autowired
    PracticeSessionQuestionService practiceSessionQuestionService;

    @Autowired
    ReviewItemService reviewItemService;

    @Autowired
    WrongQuestionService wrongQuestionService;

    @Autowired
    QuestionBankService questionBankService;

    /**
     * 预览当前组卷规则可生成的题目，预览不写入任何练习数据。
     */
    @Override
    public PracticeSessionPreviewResp previewPracticeSession(PracticeSessionCreateReq request) {
        Long userId = AccountUtils.getUserId();
        PracticeSelection selection = selectCandidates(userId, request);
        PracticeSessionPreviewResp response = new PracticeSessionPreviewResp();
        response.setRequestedQuestionCount(request.getQuestionCount());
        response.setAvailableQuestionCount(selection.candidates.size());
        response.setShortageQuestionCount(Math.max(0,
                request.getQuestionCount() - selection.candidates.size()));
        response.setWrongQuestionCount((int) selection.candidates.stream()
                .filter(candidate -> PracticeQuestionSource.WRONG_QUESTION.equals(candidate.questionSource))
                .count());
        response.setBankQuestionCount((int) selection.candidates.stream()
                .filter(candidate -> PracticeQuestionSource.QUESTION_BANK.equals(candidate.questionSource))
                .count());
        response.setLearningPointList(selection.candidates.stream()
                .map(PracticeCandidate::learningPoint)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList()));
        response.setGenerationReason(buildGenerationReason(request, selection.source));
        return response;
    }

    /**
     * 创建专项练习；题量不足时不创建不完整会话。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PracticeSessionDetailResp createPracticeSession(PracticeSessionCreateReq request) {
        Long userId = AccountUtils.getUserId();
        PracticeSelection selection = selectCandidates(userId, request);
        if (selection.candidates.size() < request.getQuestionCount()) {
            throw new LogicException(ErrorCodeConstants.PRACTICE_SESSION_QUESTION_INSUFFICIENT);
        }
        List<PracticeCandidate> candidates = selection.candidates;
        LocalDateTime now = LocalDateTime.now();

        PracticeSession session = new PracticeSession();
        session.setUserId(userId);
        session.setPracticeType(request.getPracticeType());
        session.setTitle(buildTitle(request));
        session.setGenerationReason(buildGenerationReason(request, selection.source));
        session.setSubject(request.getSubject());
        session.setSubjectName(candidates.isEmpty() ? "" : candidates.get(0).subjectName());
        session.setLearningPoint(request.getLearningPoint());
        session.setErrorLabel(request.getErrorLabel());
        session.setDifficulty(request.getDifficulty());
        session.setQuestionCount(candidates.size());
        session.setAnsweredCount(0);
        session.setCorrectCount(0);
        session.setWrongCount(0);
        session.setTotalDurationSeconds(0);
        session.setAccuracyRate(0);
        session.setStatus(PracticeSessionStatus.IN_PROGRESS);
        session.setStartTime(now);
        session.setCreateTime(now);
        session.setUpdateTime(now);
        practiceSessionService.save(session);

        int sortNo = 1;
        for (PracticeCandidate candidate : candidates) {
            PracticeSessionQuestion sessionQuestion = new PracticeSessionQuestion();
            sessionQuestion.setSessionId(session.getId());
            sessionQuestion.setUserId(userId);
            sessionQuestion.setWrongQuestionId(candidate.wrongQuestionId());
            sessionQuestion.setQuestionSource(candidate.questionSource);
            sessionQuestion.setBankQuestionId(candidate.bankQuestionId());
            sessionQuestion.setSortNo(sortNo++);
            sessionQuestion.setQuestionTitleSnapshot(candidate.questionTitle());
            sessionQuestion.setSourceReason(candidate.sourceReason());
            sessionQuestion.setSubject(candidate.subject());
            sessionQuestion.setSubjectName(candidate.subjectName());
            sessionQuestion.setLearningPoint(candidate.learningPoint());
            sessionQuestion.setErrorLabelSnapshot(candidate.errorLabels());
            sessionQuestion.setDifficulty(candidate.difficulty());
            sessionQuestion.setCreateTime(now);
            practiceSessionQuestionService.save(sessionQuestion);
        }
        List<PracticeSessionQuestion> sessionQuestions = practiceSessionQuestionService.lambdaQuery()
                .eq(PracticeSessionQuestion::getSessionId, session.getId())
                .orderByAsc(PracticeSessionQuestion::getSortNo)
                .list();
        return PracticeSessionSupport.buildDetail(session, sessionQuestions,
                selection.wrongQuestions, selection.bankQuestions);
    }

    /**
     * 按请求选择候选题并保留选择过程中的题源信息，供预览和创建共用。
     */
    private PracticeSelection selectCandidates(Long userId, PracticeSessionCreateReq request) {
        String source = StringUtils.hasText(request.getQuestionSource())
                ? request.getQuestionSource() : PracticeQuestionSource.WRONG_QUESTION;
        List<WrongQuestion> wrongQuestions = PracticeQuestionSource.includeWrongQuestion(source)
                ? selectQuestions(userId, request) : Collections.emptyList();
        List<QuestionBank> bankQuestions = PracticeQuestionSource.includeQuestionBank(source)
                ? selectBankQuestions(userId, request, wrongQuestions) : Collections.emptyList();
        List<PracticeCandidate> candidates = mergeCandidates(source, request.getQuestionCount(),
                wrongQuestions, bankQuestions);
        return new PracticeSelection(source, wrongQuestions, bankQuestions, candidates);
    }

    /**
     * 查询业务数据。
     */
    private List<WrongQuestion> selectQuestions(Long userId, PracticeSessionCreateReq request) {
        int questionCount = request.getQuestionCount() == null ? 5 : request.getQuestionCount();
        int candidateLimit = Math.max(questionCount * 8, 80);
        List<WrongQuestion> candidates = wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getCreateId, userId)
                .in(Boolean.TRUE.equals(request.getIncludeMastered()), WrongQuestion::getStatus, 1, 2)
                .eq(!Boolean.TRUE.equals(request.getIncludeMastered()), WrongQuestion::getStatus, 1)
                .eq(StringUtils.hasText(request.getSubject()), WrongQuestion::getSubject,
                        request.getSubject())
                .eq(StringUtils.hasText(request.getLearningPoint()), WrongQuestion::getLearningPoint,
                        request.getLearningPoint())
                .like(StringUtils.hasText(request.getErrorLabel()), WrongQuestion::getErrorLabels,
                        request.getErrorLabel())
                .eq(request.getDifficulty() != null, WrongQuestion::getLevel, request.getDifficulty())
                .orderByAsc(WrongQuestion::getStatus)
                .orderByDesc(WrongQuestion::getLevel)
                .orderByDesc(WrongQuestion::getUpdateTime)
                .last("LIMIT " + candidateLimit)
                .list();
        candidates = deduplicateWrongQuestions(candidates);
        if (candidates.isEmpty()) {
            return candidates;
        }
        List<Long> wrongQuestionIds = candidates.stream().map(WrongQuestion::getId)
                .collect(Collectors.toList());
        Map<Long, Integer> masteryScoreMap = reviewItemService.lambdaQuery()
                .eq(ReviewItem::getUserId, userId)
                .in(ReviewItem::getWrongQuestionId, wrongQuestionIds)
                .list().stream()
                .collect(Collectors.toMap(ReviewItem::getWrongQuestionId,
                        item -> item.getMasteryScore() == null ? 40 : item.getMasteryScore(),
                        (left, right) -> left));
        Map<Long, LocalDateTime> lastPracticeTimeMap = practiceSessionQuestionService.lambdaQuery()
                .eq(PracticeSessionQuestion::getUserId, userId)
                .in(PracticeSessionQuestion::getWrongQuestionId, wrongQuestionIds)
                .isNotNull(PracticeSessionQuestion::getAnswerTime)
                .list().stream()
                .collect(Collectors.toMap(PracticeSessionQuestion::getWrongQuestionId,
                        PracticeSessionQuestion::getAnswerTime,
                        (left, right) -> left.isAfter(right) ? left : right));
        LocalDateTime recentThreshold = LocalDateTime.now().minusDays(7);
        Set<Long> recentPracticeIds = lastPracticeTimeMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBefore(recentThreshold))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        List<WrongQuestion> preferredQuestions = candidates.stream()
                .filter(item -> !recentPracticeIds.contains(item.getId()))
                .sorted(questionComparator(masteryScoreMap, lastPracticeTimeMap))
                .limit(questionCount)
                .collect(Collectors.toList());
        if (preferredQuestions.size() >= questionCount) {
            return preferredQuestions;
        }
        Set<Long> selectedIds = preferredQuestions.stream().map(WrongQuestion::getId)
                .collect(Collectors.toSet());
        List<WrongQuestion> fallbackQuestions = candidates.stream()
                .filter(item -> !selectedIds.contains(item.getId()))
                .sorted(questionComparator(masteryScoreMap, lastPracticeTimeMap))
                .limit(questionCount - preferredQuestions.size())
                .collect(Collectors.toList());
        preferredQuestions.addAll(fallbackQuestions);
        return preferredQuestions;
    }

    private List<QuestionBank> selectBankQuestions(Long userId, PracticeSessionCreateReq request,
                                                   List<WrongQuestion> wrongSeeds) {
        int questionCount = request.getQuestionCount() == null ? 5 : request.getQuestionCount();
        int candidateLimit = Math.max(questionCount * 8, 80);
        List<QuestionBank> candidates = new java.util.ArrayList<>();
        if (!wrongSeeds.isEmpty()) {
            for (WrongQuestion seed : wrongSeeds) {
                if (candidates.size() >= candidateLimit) {
                    break;
                }
                int difficulty = seed.getLevel() == null ? 3 : seed.getLevel();
                String keyword = StringUtils.hasText(seed.getLearningPoint()) ? seed.getLearningPoint()
                        : (StringUtils.hasText(seed.getQuestionTitle())
                        ? seed.getQuestionTitle() : seed.getQuestionContent());
                if (StringUtils.hasText(keyword)) {
                    candidates.addAll(questionBankService.selectSimilarCandidates(userId, seed.getGrade(),
                            seed.getSubject(), seed.getQuestionType(), difficulty, keyword, questionCount * 2));
                }
                if (candidates.size() < candidateLimit) {
                    candidates.addAll(questionBankService.selectFallbackCandidates(userId, seed.getGrade(),
                            seed.getSubject(), seed.getQuestionType(), difficulty, false, true, questionCount));
                }
            }
        }
        if (candidates.size() < questionCount) {
            candidates.addAll(questionBankService.lambdaQuery()
                    .eq(QuestionBank::getReviewStatus, 1)
                    .eq(QuestionBank::getEnable, 1)
                    .and(query -> query.isNull(QuestionBank::getExpireAt)
                            .or().ge(QuestionBank::getExpireAt, java.time.LocalDate.now()))
                    .eq(StringUtils.hasText(request.getSubject()), QuestionBank::getSubject, request.getSubject())
                    .eq(request.getDifficulty() != null, QuestionBank::getDifficulty, request.getDifficulty())
                    .and(StringUtils.hasText(request.getLearningPoint()), query -> query
                            .like(QuestionBank::getQuestionTitle, request.getLearningPoint())
                            .or().like(QuestionBank::getQuestionContent, request.getLearningPoint()))
                    .orderByAsc(QuestionBank::getDifficulty)
                    .orderByDesc(QuestionBank::getId)
                    .last("LIMIT " + candidateLimit)
                    .list());
        }
        Map<Long, LocalDateTime> lastPracticeTimeMap = practiceSessionQuestionService.lambdaQuery()
                .eq(PracticeSessionQuestion::getUserId, userId)
                .isNotNull(PracticeSessionQuestion::getBankQuestionId)
                .isNotNull(PracticeSessionQuestion::getAnswerTime)
                .list().stream()
                .collect(Collectors.toMap(PracticeSessionQuestion::getBankQuestionId,
                        PracticeSessionQuestion::getAnswerTime,
                        (left, right) -> left.isAfter(right) ? left : right));
        LocalDateTime recentThreshold = LocalDateTime.now().minusDays(7);
        Set<Long> selectedIds = new java.util.HashSet<>();
        List<QuestionBank> deduplicated = candidates.stream()
                .filter(item -> selectedIds.add(item.getId()))
                .collect(Collectors.toList());
        List<QuestionBank> preferred = deduplicated.stream()
                .filter(item -> {
                    LocalDateTime last = lastPracticeTimeMap.get(item.getId());
                    return last == null || last.isBefore(recentThreshold);
                })
                .sorted(bankQuestionComparator(lastPracticeTimeMap))
                .limit(questionCount)
                .collect(Collectors.toList());
        if (preferred.size() >= questionCount) {
            return preferred;
        }
        Set<Long> preferredIds = preferred.stream().map(QuestionBank::getId).collect(Collectors.toSet());
        preferred.addAll(deduplicated.stream()
                .filter(item -> !preferredIds.contains(item.getId()))
                .sorted(bankQuestionComparator(lastPracticeTimeMap))
                .limit(questionCount - preferred.size())
                .collect(Collectors.toList()));
        return preferred;
    }

    private List<PracticeCandidate> mergeCandidates(String source, Integer requestCount,
                                                    List<WrongQuestion> wrongQuestions,
                                                    List<QuestionBank> bankQuestions) {
        int questionCount = requestCount == null ? 5 : requestCount;
        List<PracticeCandidate> result = new ArrayList<>();
        Set<String> deduplicateKeys = new HashSet<>();
        if (PracticeQuestionSource.QUESTION_BANK.equals(source)) {
            appendDistinct(result, bankQuestions.stream().map(PracticeCandidate::of)
                    .collect(Collectors.toList()), questionCount, deduplicateKeys);
            return result;
        }
        if (PracticeQuestionSource.MIXED.equals(source)) {
            int bankTarget = Math.max(1, questionCount / 2);
            appendDistinct(result, bankQuestions.stream().map(PracticeCandidate::of)
                    .collect(Collectors.toList()), bankTarget, deduplicateKeys);
            appendDistinct(result, wrongQuestions.stream().map(PracticeCandidate::of)
                    .collect(Collectors.toList()), questionCount, deduplicateKeys);
            appendDistinct(result, bankQuestions.stream().map(PracticeCandidate::of)
                    .collect(Collectors.toList()), questionCount, deduplicateKeys);
            return result;
        }
        appendDistinct(result, wrongQuestions.stream().map(PracticeCandidate::of)
                .collect(Collectors.toList()), questionCount, deduplicateKeys);
        return result;
    }

    /**
     * 以题干文本去重，避免题库与错题本出现同题时被重复组入。
     */
    private void appendDistinct(List<PracticeCandidate> result, List<PracticeCandidate> candidates,
                                int limit, Set<String> deduplicateKeys) {
        for (PracticeCandidate candidate : candidates) {
            if (result.size() >= limit) {
                return;
            }
            if (deduplicateKeys.add(candidate.deduplicateKey())) {
                result.add(candidate);
            }
        }
    }

    private List<WrongQuestion> deduplicateWrongQuestions(List<WrongQuestion> questions) {
        Set<String> keys = new HashSet<>();
        return questions.stream()
                .filter(question -> keys.add(normalizeQuestionText(question.getQuestionTitle(),
                        question.getQuestionContent(), question.getId())))
                .collect(Collectors.toList());
    }

    private String normalizeQuestionText(String title, String content, Long id) {
        String text = (StringUtils.hasText(title) ? title : "") + "\n"
                + (StringUtils.hasText(content) ? content : "");
        String normalized = text.replaceAll("\\s+", "").trim().toLowerCase();
        return StringUtils.hasText(normalized) ? normalized : "ID:" + id;
    }

    private Comparator<WrongQuestion> questionComparator(Map<Long, Integer> masteryScoreMap,
                                                        Map<Long, LocalDateTime> lastPracticeTimeMap) {
        return Comparator
                .comparing((WrongQuestion item) -> masteryScoreMap.getOrDefault(item.getId(), 40))
                .thenComparing(item -> lastPracticeTimeMap.get(item.getId()) == null ? 0 : 1)
                .thenComparing(item -> lastPracticeTimeMap.get(item.getId()),
                        Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing((WrongQuestion item) -> item.getLevel() == null ? 0 : item.getLevel(),
                        Comparator.reverseOrder())
                .thenComparing(WrongQuestion::getUpdateTime,
                        Comparator.nullsLast(Comparator.reverseOrder()));
    }

    /**
     * 执行 bankQuestionComparator 辅助处理。
     */
    private Comparator<QuestionBank> bankQuestionComparator(Map<Long, LocalDateTime> lastPracticeTimeMap) {
        return Comparator
                .comparing((QuestionBank item) -> lastPracticeTimeMap.get(item.getId()) == null ? 0 : 1)
                .thenComparing(item -> lastPracticeTimeMap.get(item.getId()),
                        Comparator.nullsFirst(Comparator.naturalOrder()))
                .thenComparing(item -> item.getDifficulty() == null ? 3 : item.getDifficulty())
                .thenComparing(QuestionBank::getId, Comparator.reverseOrder());
    }

    /**
     * 构建业务处理结果。
     */
    private String buildTitle(PracticeSessionCreateReq request) {
        if (StringUtils.hasText(request.getLearningPoint())) {
            return request.getLearningPoint().trim() + "专项练习";
        }
        if (StringUtils.hasText(request.getErrorLabel())) {
            return request.getErrorLabel().trim() + "错因练习";
        }
        if (StringUtils.hasText(request.getSubject())) {
            return "科目专项练习";
        }
        return "典型错题练习";
    }

    private String buildGenerationReason(PracticeSessionCreateReq request, String source) {
        List<String> conditions = new ArrayList<>();
        conditions.add("题源：" + sourceLabel(source));
        conditions.add(Boolean.TRUE.equals(request.getIncludeMastered()) ? "已掌握：包含" : "已掌握：排除");
        conditions.add("近7日已练题目：降权");
        conditions.add("重复题：排除");
        if (StringUtils.hasText(request.getSubject())) {
            conditions.add("科目：" + request.getSubject().trim());
        }
        if (StringUtils.hasText(request.getLearningPoint())) {
            conditions.add("知识点：" + request.getLearningPoint().trim());
        }
        if (StringUtils.hasText(request.getErrorLabel())) {
            conditions.add("错因：" + request.getErrorLabel().trim());
        }
        if (request.getDifficulty() != null) {
            conditions.add("难度：" + request.getDifficulty());
        }
        return String.join("；", conditions);
    }

    private String sourceLabel(String source) {
        if (PracticeQuestionSource.QUESTION_BANK.equals(source)) {
            return "题库";
        }
        if (PracticeQuestionSource.MIXED.equals(source)) {
            return "混合";
        }
        return "错题本";
    }

    private static class PracticeCandidate {
        private final String questionSource;
        private final WrongQuestion wrongQuestion;
        private final QuestionBank bankQuestion;

        /**
         * 执行 PracticeCandidate 辅助处理。
         */
        private PracticeCandidate(String questionSource, WrongQuestion wrongQuestion, QuestionBank bankQuestion) {
            this.questionSource = questionSource;
            this.wrongQuestion = wrongQuestion;
            this.bankQuestion = bankQuestion;
        }

        /**
         * 执行 of 辅助处理。
         */
        private static PracticeCandidate of(WrongQuestion question) {
            return new PracticeCandidate(PracticeQuestionSource.WRONG_QUESTION, question, null);
        }

        /**
         * 执行 of 辅助处理。
         */
        private static PracticeCandidate of(QuestionBank question) {
            return new PracticeCandidate(PracticeQuestionSource.QUESTION_BANK, null, question);
        }

        /**
         * 执行 wrongQuestionId 辅助处理。
         */
        private Long wrongQuestionId() {
            return wrongQuestion == null ? null : wrongQuestion.getId();
        }

        /**
         * 执行 bankQuestionId 辅助处理。
         */
        private Long bankQuestionId() {
            return bankQuestion == null ? null : bankQuestion.getId();
        }

        /**
         * 执行 questionTitle 辅助处理。
         */
        private String questionTitle() {
            return wrongQuestion == null ? bankQuestion.getQuestionTitle() : wrongQuestion.getQuestionTitle();
        }

        /**
         * 执行 subject 辅助处理。
         */
        private String subject() {
            return wrongQuestion == null ? bankQuestion.getSubject() : wrongQuestion.getSubject();
        }

        /**
         * 执行 subjectName 辅助处理。
         */
        private String subjectName() {
            return wrongQuestion == null ? bankQuestion.getSubjectName() : wrongQuestion.getSubjectName();
        }

        /**
         * 执行 learningPoint 辅助处理。
         */
        private String learningPoint() {
            return wrongQuestion == null ? null : wrongQuestion.getLearningPoint();
        }

        /**
         * 执行 difficulty 辅助处理。
         */
        private Integer difficulty() {
            return wrongQuestion == null ? bankQuestion.getDifficulty() : wrongQuestion.getLevel();
        }

        /**
         * 获取错因标签快照。
         */
        private String errorLabels() {
            return wrongQuestion == null ? null : wrongQuestion.getErrorLabels();
        }

        /**
         * 获取本题被选入练习的依据。
         */
        private String sourceReason() {
            if (wrongQuestion == null) {
                return "题库补充题，匹配当前筛选条件";
            }
            String reason = wrongQuestion.getStatus() != null && wrongQuestion.getStatus() == 2
                    ? "已掌握错题，按设置纳入" : "已订正错题，优先巩固";
            if (StringUtils.hasText(wrongQuestion.getErrorLabels())) {
                reason += "；错因：" + wrongQuestion.getErrorLabels();
            }
            return reason;
        }

        /**
         * 获取跨题源去重键。
         */
        private String deduplicateKey() {
            String title = wrongQuestion == null ? bankQuestion.getQuestionTitle() : wrongQuestion.getQuestionTitle();
            String content = wrongQuestion == null ? bankQuestion.getQuestionContent() : wrongQuestion.getQuestionContent();
            String normalized = ((StringUtils.hasText(title) ? title : "") + "\n"
                    + (StringUtils.hasText(content) ? content : ""))
                    .replaceAll("\\s+", "").trim().toLowerCase();
            if (StringUtils.hasText(normalized)) {
                return normalized;
            }
            return questionSource + ":" + (wrongQuestion == null ? bankQuestion.getId() : wrongQuestion.getId());
        }
    }

    /**
     * 组卷过程中的候选题与题源数据。
     */
    private static class PracticeSelection {
        private final String source;
        private final List<WrongQuestion> wrongQuestions;
        private final List<QuestionBank> bankQuestions;
        private final List<PracticeCandidate> candidates;

        private PracticeSelection(String source, List<WrongQuestion> wrongQuestions,
                                  List<QuestionBank> bankQuestions, List<PracticeCandidate> candidates) {
            this.source = source;
            this.wrongQuestions = wrongQuestions;
            this.bankQuestions = bankQuestions;
            this.candidates = candidates;
        }
    }
}
