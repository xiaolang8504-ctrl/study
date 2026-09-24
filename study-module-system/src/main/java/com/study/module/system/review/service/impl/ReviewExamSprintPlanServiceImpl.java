package com.study.module.system.review.service.impl;

import com.study.module.system.review.dto.request.ReviewExamSprintSaveReq;
import com.study.module.system.review.dto.response.ReviewExamSprintResp;
import com.study.module.system.review.entity.ReviewExamSprint;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.service.ReviewExamSprintPlanService;
import com.study.module.system.review.service.ReviewExamSprintService;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 只生成考前短练建议；常规 ReviewItem 的到期时间与状态绝不在此修改。 */
@Service
public class ReviewExamSprintPlanServiceImpl implements ReviewExamSprintPlanService {
    @Autowired private ReviewExamSprintService reviewExamSprintService;
    @Autowired private WrongQuestionService wrongQuestionService;
    @Autowired private ReviewItemService reviewItemService;

    @Override
    public ReviewExamSprintResp reviewExamSprint(String subject) {
        Long userId = AccountUtils.getUserId();
        ReviewExamSprint sprint = reviewExamSprintService.lambdaQuery().eq(ReviewExamSprint::getUserId, userId)
                .eq(StringUtils.hasText(subject), ReviewExamSprint::getSubject, subject)
                .eq(ReviewExamSprint::getStatus, 1).orderByDesc(ReviewExamSprint::getUpdateTime).last("LIMIT 1").one();
        return sprint == null ? null : buildResponse(sprint);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewExamSprintResp saveReviewExamSprint(ReviewExamSprintSaveReq request) {
        if (request.getExamDate().isBefore(LocalDate.now())) throw new IllegalArgumentException("考试日期不能早于今天");
        Long userId = AccountUtils.getUserId();
        ReviewExamSprint sprint = reviewExamSprintService.lambdaQuery().eq(ReviewExamSprint::getUserId, userId)
                .eq(ReviewExamSprint::getSubject, request.getSubject().trim()).one();
        LocalDateTime now = LocalDateTime.now();
        if (sprint == null) { sprint = new ReviewExamSprint(); sprint.setUserId(userId); sprint.setSubject(request.getSubject().trim()); sprint.setCreateTime(now); }
        sprint.setExamDate(request.getExamDate()); sprint.setScopeText(StringUtils.hasText(request.getScopeText()) ? request.getScopeText().trim() : null);
        sprint.setDailyMinutes(request.getDailyMinutes()); sprint.setTargetQuestionCount(request.getTargetQuestionCount()); sprint.setStatus(1); sprint.setUpdateTime(now);
        reviewExamSprintService.saveOrUpdate(sprint);
        return buildResponse(sprint);
    }

    private ReviewExamSprintResp buildResponse(ReviewExamSprint sprint) {
        Long userId = sprint.getUserId();
        List<WrongQuestion> questions = wrongQuestionService.lambdaQuery().eq(WrongQuestion::getCreateId, userId)
                .isNull(WrongQuestion::getMergedToId).eq(WrongQuestion::getSubject, sprint.getSubject())
                .in(WrongQuestion::getStatus, 0, 1).orderByAsc(WrongQuestion::getStatus)
                .orderByDesc(WrongQuestion::getPriorityLevel).orderByDesc(WrongQuestion::getLevel).orderByDesc(WrongQuestion::getUpdateTime).last("LIMIT 120").list();
        questions = filterScope(questions, sprint.getScopeText());
        Map<Long, ReviewItem> reviewItems = questions.isEmpty() ? Collections.emptyMap() : reviewItemService.lambdaQuery()
                .eq(ReviewItem::getUserId, userId).in(ReviewItem::getWrongQuestionId, questions.stream().map(WrongQuestion::getId).collect(Collectors.toList()))
                .list().stream().collect(Collectors.toMap(ReviewItem::getWrongQuestionId, Function.identity(), (left, right) -> left));
        List<WrongQuestion> selected = questions.stream().sorted(Comparator.<WrongQuestion>comparingInt(question -> sprintScore(question, reviewItems.get(question.getId()))).reversed())
                .limit(sprint.getTargetQuestionCount()).collect(Collectors.toList());
        ReviewExamSprintResp response = new ReviewExamSprintResp(); response.setId(sprint.getId()); response.setSubject(sprint.getSubject()); response.setExamDate(sprint.getExamDate()); response.setScopeText(sprint.getScopeText()); response.setDailyMinutes(sprint.getDailyMinutes()); response.setTargetQuestionCount(sprint.getTargetQuestionCount());
        response.setDaysRemaining((int) Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), sprint.getExamDate()))); response.setEstimatedMinutes(Math.min(sprint.getDailyMinutes(), Math.max(5, selected.size() * 5)));
        response.setCoordinationNote("冲刺短练仅额外推荐未订正或掌握不稳题；常规到期复习仍按原计划优先显示。");
        response.setCandidates(selected.stream().map(question -> candidate(question, reviewItems.get(question.getId()))).collect(Collectors.toList())); return response;
    }

    private int sprintScore(WrongQuestion question, ReviewItem item) {
        int score = Integer.valueOf(0).equals(question.getStatus()) ? 1000 : 400;
        score += (question.getPriorityLevel() == null ? 0 : question.getPriorityLevel() * 25) + (question.getLevel() == null ? 0 : question.getLevel() * 10);
        return score + (item == null || item.getMasteryScore() == null ? 60 : Math.max(0, 100 - item.getMasteryScore()));
    }

    /** 范围以逗号、顿号或换行拆分，命中知识点、章节或题干才进入冲刺候选。 */
    private List<WrongQuestion> filterScope(List<WrongQuestion> questions, String scopeText) {
        if (!StringUtils.hasText(scopeText)) return questions;
        List<String> tokens = java.util.Arrays.stream(scopeText.split("[,，、;；\\n\\r]+"))
                .map(String::trim).filter(StringUtils::hasText).collect(Collectors.toList());
        if (tokens.isEmpty()) return questions;
        return questions.stream().filter(question -> {
            String searchable = String.join(" ", safe(question.getLearningPoint()), safe(question.getChapterName()),
                    safe(question.getQuestionTitle()), safe(question.getQuestionContent()));
            return tokens.stream().anyMatch(searchable::contains);
        }).collect(Collectors.toList());
    }

    private String safe(String value) { return value == null ? "" : value; }

    private ReviewExamSprintResp.Candidate candidate(WrongQuestion question, ReviewItem item) {
        ReviewExamSprintResp.Candidate candidate = new ReviewExamSprintResp.Candidate(); candidate.setWrongQuestionId(question.getId()); candidate.setQuestionTitle(question.getQuestionTitle()); candidate.setLearningPoint(question.getLearningPoint()); candidate.setEstimatedMinutes(5);
        candidate.setPriorityReason(Integer.valueOf(0).equals(question.getStatus()) ? "尚未订正，考前优先补齐" : item == null || item.getMasteryScore() == null || item.getMasteryScore() < 60 ? "已订正但掌握不稳" : "高优先级错题，适合考前巩固"); return candidate;
    }
}
