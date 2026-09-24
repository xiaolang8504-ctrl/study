package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.wrongquestion.convert.WrongQuestionConvert;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionPageListReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionPageListResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.entity.WrongQuestionCorrectionRecord;
import com.study.module.system.wrongquestion.entity.WrongQuestionOccurrence;
import com.study.module.system.wrongquestion.entity.WrongQuestionTag;
import com.study.module.system.wrongquestion.entity.WrongQuestionTagRelation;
import com.study.module.system.wrongquestion.mapper.WrongQuestionMapper;
import com.study.module.system.wrongquestion.mapper.WrongQuestionCorrectionRecordMapper;
import com.study.module.system.wrongquestion.mapper.WrongQuestionOccurrenceMapper;
import com.study.module.system.wrongquestion.mapper.WrongQuestionTagMapper;
import com.study.module.system.wrongquestion.mapper.WrongQuestionTagRelationMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionOrganizeService;
import com.study.module.system.wrongquestion.service.WrongQuestionListService;
import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.questionbank.entity.WrongQuestionKnowledgePoint;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.service.ReviewItemService;
import com.study.module.system.user.service.UserService;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.study.module.system.questionbank.service.WrongQuestionKnowledgePointService;
import com.study.module.system.questionbank.service.KnowledgePointService;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 初中生错题列表服务
 */
@Service
public class WrongQuestionListServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion> implements WrongQuestionListService {

    @Autowired
    WrongQuestionKnowledgePointService wrongQuestionKnowledgePointService;

    @Autowired
    KnowledgePointService knowledgePointService;

    @Autowired
    UserService userService;

    @Autowired
    WrongQuestionOrganizeService wrongQuestionOrganizeService;

    @Autowired
    WrongQuestionOccurrenceMapper wrongQuestionOccurrenceMapper;

    @Autowired
    WrongQuestionCorrectionRecordMapper wrongQuestionCorrectionRecordMapper;

    @Autowired
    ReviewItemService reviewItemService;

    @Autowired
    WrongQuestionTagMapper wrongQuestionTagMapper;

    @Autowired
    WrongQuestionTagRelationMapper wrongQuestionTagRelationMapper;

    /**
     * 错题分页列表
     */
    @Override
    public PageResult<WrongQuestionPageListResp> wrongQuestionPageList(WrongQuestionPageListReq request) {
        LambdaQueryWrapper<WrongQuestion> queryWrapper = getWrongQuestionListQueryWrapper(request);
        Page<WrongQuestion> page = new Page<>(request.getCurrent(), request.getPageSize());
        this.page(page, queryWrapper);
        PageResult<WrongQuestionPageListResp> result = PageUtils.wrap(page, WrongQuestionConvert.INSTANCE::toWrongQuestionPageListResp);
        List<Long> wrongQuestionIds = page.getRecords().stream().map(WrongQuestion::getId).collect(Collectors.toList());
        Map<Long, List<String>> tagNames = wrongQuestionOrganizeService.resolveTagNames(wrongQuestionIds);
        Map<Long, WrongQuestionOccurrenceSummary> occurrenceSummaries = resolveOccurrenceSummaries(wrongQuestionIds);
        Map<Long, LocalDateTime> correctionTimes = resolveLatestCorrectionTimes(wrongQuestionIds);
        Map<Long, ReviewItem> reviewItems = resolveReviewItems(wrongQuestionIds);
        for (int i = 0; i < page.getRecords().size(); i++) {
            WrongQuestion wrongQuestion = page.getRecords().get(i);
            WrongQuestionPageListResp response = result.getList().get(i);
            List<Long> pointIds = wrongQuestionKnowledgePointService.resolvePointIds(page.getRecords().get(i));
            response.setKnowledgePointNames(pointIds.isEmpty() ? Collections.emptyList()
                    : knowledgePointService.listByIds(pointIds).stream()
                    .map(item -> item.getPointName()).collect(Collectors.toList()));
            response.setTagNames(tagNames.getOrDefault(wrongQuestion.getId(), Collections.emptyList()));
            WrongQuestionOccurrenceSummary summary = occurrenceSummaries.get(wrongQuestion.getId());
            response.setWrongOccurrenceCount(summary == null ? 1 : summary.count);
            response.setLatestWrongTime(summary == null ? wrongQuestion.getCreateTime() : summary.latestTime);
            response.setLatestCorrectionTime(correctionTimes.get(wrongQuestion.getId()));
            response.setReviewStatus(resolveReviewStatus(wrongQuestion, reviewItems.get(wrongQuestion.getId())));
        }
        return result;
    }

    /**
     * 获取错题列表查询
     */
    private LambdaQueryWrapper<WrongQuestion> getWrongQuestionListQueryWrapper(WrongQuestionPageListReq request) {
        LambdaQueryWrapper<WrongQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WrongQuestion::getCreateId, userService.getUserIdByToken());
        queryWrapper.isNull(WrongQuestion::getMergedToId);
        if (StringUtils.hasText(request.getGrade())) {
            queryWrapper.eq(WrongQuestion::getGrade, request.getGrade());
        }
        if (StringUtils.hasText(request.getSubject())) {
            queryWrapper.eq(WrongQuestion::getSubject, request.getSubject());
        }
        if (StringUtils.hasText(request.getQuestionType())) {
            queryWrapper.eq(WrongQuestion::getQuestionType, request.getQuestionType());
        }
        if (StringUtils.hasText(request.getSource())) {
            queryWrapper.eq(WrongQuestion::getSource, request.getSource());
        }
        if (request.getStatus() != null) {
            queryWrapper.eq(WrongQuestion::getStatus, request.getStatus());
        }
        if (request.getKnowledgePointId() != null) {
            List<Long> wrongQuestionIds = wrongQuestionKnowledgePointService.lambdaQuery()
                    .eq(WrongQuestionKnowledgePoint::getKnowledgePointId, request.getKnowledgePointId())
                    .list()
                    .stream()
                    .map(WrongQuestionKnowledgePoint::getWrongQuestionId)
                    .collect(Collectors.toList());
            if (wrongQuestionIds.isEmpty()) {
                queryWrapper.eq(WrongQuestion::getId, -1L);
            } else {
                queryWrapper.in(WrongQuestion::getId, wrongQuestionIds);
            }
        }
        if (StringUtils.hasText(request.getErrorLabel())) {
            queryWrapper.like(WrongQuestion::getErrorLabels, request.getErrorLabel());
        }
        if (StringUtils.hasText(request.getErrorCauseCode())) {
            queryWrapper.like(WrongQuestion::getErrorCauseCodes, request.getErrorCauseCode());
        }
        if (StringUtils.hasText(request.getAbilityLevel())) {
            queryWrapper.eq(WrongQuestion::getAbilityLevel, request.getAbilityLevel());
        }
        if (StringUtils.hasText(request.getTextbookVersion())) {
            queryWrapper.like(WrongQuestion::getTextbookVersion, request.getTextbookVersion().trim());
        }
        if (StringUtils.hasText(request.getChapterName())) {
            queryWrapper.like(WrongQuestion::getChapterName, request.getChapterName().trim());
        }
        if (request.getFavorite() != null) {
            queryWrapper.eq(WrongQuestion::getFavorite, request.getFavorite());
        }
        if (request.getPriorityLevel() != null) {
            queryWrapper.eq(WrongQuestion::getPriorityLevel, request.getPriorityLevel());
        }
        if (StringUtils.hasText(request.getTagName())) {
            List<Long> tagIds = wrongQuestionTagMapper.selectList(new LambdaQueryWrapper<WrongQuestionTag>()
                            .eq(WrongQuestionTag::getUserId, userService.getUserIdByToken())
                            .like(WrongQuestionTag::getTagName, request.getTagName().trim()))
                    .stream().map(WrongQuestionTag::getId).collect(Collectors.toList());
            if (tagIds.isEmpty()) {
                queryWrapper.eq(WrongQuestion::getId, -1L);
            } else {
                List<Long> wrongQuestionIds = wrongQuestionTagRelationMapper.selectList(
                                new LambdaQueryWrapper<WrongQuestionTagRelation>()
                                        .eq(WrongQuestionTagRelation::getUserId, userService.getUserIdByToken())
                                        .in(WrongQuestionTagRelation::getTagId, tagIds))
                        .stream().map(WrongQuestionTagRelation::getWrongQuestionId).distinct().collect(Collectors.toList());
                if (wrongQuestionIds.isEmpty()) {
                    queryWrapper.eq(WrongQuestion::getId, -1L);
                } else {
                    queryWrapper.in(WrongQuestion::getId, wrongQuestionIds);
                }
            }
        }
        if (StringUtils.hasText(request.getKeyWord())) {
            queryWrapper.and(wrapper -> wrapper.like(WrongQuestion::getQuestionTitle, request.getKeyWord())
                    .or()
                    .like(WrongQuestion::getQuestionContent, request.getKeyWord()));
        }
        queryWrapper.orderByDesc(WrongQuestion::getId);
        return queryWrapper;
    }

    private Map<Long, WrongQuestionOccurrenceSummary> resolveOccurrenceSummaries(List<Long> wrongQuestionIds) {
        if (wrongQuestionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, WrongQuestionOccurrenceSummary> result = new HashMap<>();
        for (WrongQuestionOccurrence occurrence : wrongQuestionOccurrenceMapper.selectList(
                new LambdaQueryWrapper<WrongQuestionOccurrence>().in(WrongQuestionOccurrence::getCanonicalQuestionId, wrongQuestionIds))) {
            WrongQuestionOccurrenceSummary summary = result.computeIfAbsent(occurrence.getCanonicalQuestionId(), key -> new WrongQuestionOccurrenceSummary());
            summary.count++;
            LocalDateTime occurredAt = occurrence.getOccurredAt() == null ? occurrence.getCreateTime() : occurrence.getOccurredAt();
            if (summary.latestTime == null || (occurredAt != null && occurredAt.isAfter(summary.latestTime))) {
                summary.latestTime = occurredAt;
            }
        }
        return result;
    }

    private Map<Long, LocalDateTime> resolveLatestCorrectionTimes(List<Long> wrongQuestionIds) {
        if (wrongQuestionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, LocalDateTime> result = new HashMap<>();
        for (WrongQuestionCorrectionRecord record : wrongQuestionCorrectionRecordMapper.selectList(
                new LambdaQueryWrapper<WrongQuestionCorrectionRecord>().in(WrongQuestionCorrectionRecord::getWrongQuestionId, wrongQuestionIds))) {
            LocalDateTime previous = result.get(record.getWrongQuestionId());
            if (previous == null || (record.getCreateTime() != null && record.getCreateTime().isAfter(previous))) {
                result.put(record.getWrongQuestionId(), record.getCreateTime());
            }
        }
        return result;
    }

    private Map<Long, ReviewItem> resolveReviewItems(List<Long> wrongQuestionIds) {
        if (wrongQuestionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return reviewItemService.lambdaQuery().eq(ReviewItem::getUserId, userService.getUserIdByToken())
                .in(ReviewItem::getWrongQuestionId, wrongQuestionIds).list().stream()
                .collect(Collectors.toMap(ReviewItem::getWrongQuestionId, item -> item, (left, right) -> left));
    }

    private String resolveReviewStatus(WrongQuestion wrongQuestion, ReviewItem reviewItem) {
        if (Integer.valueOf(3).equals(wrongQuestion.getStatus())) return "已归档";
        if (Integer.valueOf(0).equals(wrongQuestion.getStatus())) return "待订正";
        if (reviewItem == null) return Integer.valueOf(2).equals(wrongQuestion.getStatus()) ? "已掌握" : "待加入复习";
        if (Integer.valueOf(2).equals(reviewItem.getItemStatus())) return "复习已结束";
        if (Integer.valueOf(1).equals(reviewItem.getItemStatus())) return "复习已暂停";
        if (reviewItem.getNextReviewTime() != null && !reviewItem.getNextReviewTime().isAfter(LocalDateTime.now())) return "待复习";
        return "复习中";
    }

    private static class WrongQuestionOccurrenceSummary {
        private int count;
        private LocalDateTime latestTime;
    }
}
