package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.entity.KnowledgePoint;
import com.study.module.system.questionbank.service.KnowledgePointService;
import com.study.module.system.questionbank.service.WrongQuestionKnowledgePointService;
import com.study.module.system.review.service.ReviewEnrollmentService;
import com.study.module.system.user.service.UserService;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionBatchOrganizeReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionSavedFilterSaveReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionSavedFilterResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionTagResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.entity.WrongQuestionSavedFilter;
import com.study.module.system.wrongquestion.entity.WrongQuestionTag;
import com.study.module.system.wrongquestion.entity.WrongQuestionTagRelation;
import com.study.module.system.wrongquestion.mapper.WrongQuestionMapper;
import com.study.module.system.wrongquestion.mapper.WrongQuestionSavedFilterMapper;
import com.study.module.system.wrongquestion.mapper.WrongQuestionTagMapper;
import com.study.module.system.wrongquestion.mapper.WrongQuestionTagRelationMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionOrganizeService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 错题整理：教材章节、个人标签、收藏优先级和常用筛选。 */
@Service
public class WrongQuestionOrganizeServiceImpl implements WrongQuestionOrganizeService {

    private static final int MAX_PRIORITY = 5;

    @Autowired
    private WrongQuestionMapper wrongQuestionMapper;
    @Autowired
    private WrongQuestionTagMapper wrongQuestionTagMapper;
    @Autowired
    private WrongQuestionTagRelationMapper wrongQuestionTagRelationMapper;
    @Autowired
    private WrongQuestionSavedFilterMapper wrongQuestionSavedFilterMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private KnowledgePointService knowledgePointService;
    @Autowired
    private WrongQuestionKnowledgePointService wrongQuestionKnowledgePointService;
    @Autowired
    private ReviewEnrollmentService reviewEnrollmentService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchOrganizeWrongQuestion(WrongQuestionBatchOrganizeReq request) {
        Set<Long> ids = new LinkedHashSet<>(request.getIds());
        if (ids.size() != request.getIds().size() || ids.contains(null) || !hasAnyChange(request)) {
            throw new LogicException(ErrorCodeConstants.UPDATE_WRONG_QUESTION_FAIL);
        }
        if (request.getPriorityLevel() != null
                && (request.getPriorityLevel() < 0 || request.getPriorityLevel() > MAX_PRIORITY)) {
            throw new LogicException(ErrorCodeConstants.UPDATE_WRONG_QUESTION_FAIL);
        }
        Long userId = userService.getUserIdByToken();
        List<WrongQuestion> wrongQuestions = wrongQuestionMapper.selectList(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getCreateId, userId)
                .isNull(WrongQuestion::getMergedToId)
                .in(WrongQuestion::getId, ids));
        if (wrongQuestions.size() != ids.size()) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_NOT_EXIST);
        }
        List<String> tagNames = request.getTagNames() == null ? null : normalizeTagNames(request.getTagNames());
        LocalDateTime now = LocalDateTime.now();
        for (WrongQuestion wrongQuestion : wrongQuestions) {
            if (request.getTextbookVersion() != null) {
                wrongQuestion.setTextbookVersion(trimToEmpty(request.getTextbookVersion()));
            }
            if (request.getChapterName() != null) {
                wrongQuestion.setChapterName(trimToEmpty(request.getChapterName()));
            }
            if (request.getFavorite() != null) {
                wrongQuestion.setFavorite(Boolean.TRUE.equals(request.getFavorite()) ? 1 : 0);
            }
            if (request.getPriorityLevel() != null) {
                wrongQuestion.setPriorityLevel(request.getPriorityLevel());
            }
            if (Boolean.TRUE.equals(request.getArchive())) {
                wrongQuestion.setStatus(WrongQuestionStatus.ARCHIVED);
            }
            wrongQuestion.setUpdateTime(now);
            if (wrongQuestionMapper.updateById(wrongQuestion) != 1) {
                throw new LogicException(ErrorCodeConstants.UPDATE_WRONG_QUESTION_FAIL);
            }
            if (tagNames != null) {
                rewriteTags(wrongQuestion.getId(), userId, tagNames, now);
            }
            if (request.getKnowledgePointIds() != null) {
                validateKnowledgePoints(wrongQuestion, request.getKnowledgePointIds());
                wrongQuestionKnowledgePointService.rewrite(wrongQuestion.getId(), request.getKnowledgePointIds());
            }
            if (Boolean.TRUE.equals(request.getArchive())) {
                reviewEnrollmentService.syncWrongQuestionReview(wrongQuestion);
            }
        }
    }

    @Override
    public List<WrongQuestionTagResp> wrongQuestionTagList() {
        Long userId = userService.getUserIdByToken();
        List<WrongQuestionTag> tags = wrongQuestionTagMapper.selectList(new LambdaQueryWrapper<WrongQuestionTag>()
                .eq(WrongQuestionTag::getUserId, userId)
                .orderByAsc(WrongQuestionTag::getTagName));
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> tagIds = tags.stream().map(WrongQuestionTag::getId).collect(Collectors.toList());
        Map<Long, Long> countMap = wrongQuestionTagRelationMapper.selectList(
                        new LambdaQueryWrapper<WrongQuestionTagRelation>()
                                .eq(WrongQuestionTagRelation::getUserId, userId)
                                .in(WrongQuestionTagRelation::getTagId, tagIds))
                .stream().collect(Collectors.groupingBy(WrongQuestionTagRelation::getTagId, Collectors.counting()));
        return tags.stream().map(tag -> {
            WrongQuestionTagResp response = new WrongQuestionTagResp();
            response.setId(tag.getId());
            response.setTagName(tag.getTagName());
            response.setWrongQuestionCount(countMap.getOrDefault(tag.getId(), 0L));
            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public List<WrongQuestionSavedFilterResp> savedWrongQuestionFilterList() {
        Long userId = userService.getUserIdByToken();
        return wrongQuestionSavedFilterMapper.selectList(new LambdaQueryWrapper<WrongQuestionSavedFilter>()
                        .eq(WrongQuestionSavedFilter::getUserId, userId)
                        .orderByAsc(WrongQuestionSavedFilter::getSortNo, WrongQuestionSavedFilter::getId))
                .stream().map(item -> {
                    WrongQuestionSavedFilterResp response = new WrongQuestionSavedFilterResp();
                    BeanUtils.copyProperties(item, response);
                    return response;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveWrongQuestionFilter(WrongQuestionSavedFilterSaveReq request) {
        Long userId = userService.getUserIdByToken();
        LocalDateTime now = LocalDateTime.now();
        WrongQuestionSavedFilter filter;
        if (request.getId() == null) {
            filter = new WrongQuestionSavedFilter();
            filter.setUserId(userId);
            filter.setCreateTime(now);
        } else {
            filter = wrongQuestionSavedFilterMapper.selectOne(new LambdaQueryWrapper<WrongQuestionSavedFilter>()
                    .eq(WrongQuestionSavedFilter::getId, request.getId())
                    .eq(WrongQuestionSavedFilter::getUserId, userId));
            if (filter == null) {
                throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_NOT_EXIST);
            }
        }
        filter.setFilterName(request.getFilterName().trim());
        filter.setFilterJson(request.getFilterJson().trim());
        filter.setSortNo(request.getSortNo() == null ? 0 : request.getSortNo());
        filter.setDefaultFlag(Integer.valueOf(1).equals(request.getDefaultFlag()) ? 1 : 0);
        filter.setUpdateTime(now);
        try {
            if (filter.getId() == null) {
                wrongQuestionSavedFilterMapper.insert(filter);
            } else {
                wrongQuestionSavedFilterMapper.updateById(filter);
            }
        } catch (DuplicateKeyException duplicateKeyException) {
            throw new LogicException(ErrorCodeConstants.UPDATE_WRONG_QUESTION_FAIL);
        }
        return filter.getId();
    }

    @Override
    public void deleteWrongQuestionFilter(Long id) {
        Long userId = userService.getUserIdByToken();
        if (wrongQuestionSavedFilterMapper.delete(new LambdaQueryWrapper<WrongQuestionSavedFilter>()
                .eq(WrongQuestionSavedFilter::getId, id)
                .eq(WrongQuestionSavedFilter::getUserId, userId)) != 1) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_NOT_EXIST);
        }
    }

    @Override
    public Map<Long, List<String>> resolveTagNames(List<Long> wrongQuestionIds) {
        if (wrongQuestionIds == null || wrongQuestionIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Long userId = AccountUtils.getUserId();
        List<WrongQuestionTagRelation> relations = wrongQuestionTagRelationMapper.selectList(
                new LambdaQueryWrapper<WrongQuestionTagRelation>()
                        .eq(WrongQuestionTagRelation::getUserId, userId)
                        .in(WrongQuestionTagRelation::getWrongQuestionId, wrongQuestionIds));
        if (relations.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, String> tagNameMap = wrongQuestionTagMapper.selectList(new LambdaQueryWrapper<WrongQuestionTag>()
                        .eq(WrongQuestionTag::getUserId, userId)
                        .in(WrongQuestionTag::getId, relations.stream()
                                .map(WrongQuestionTagRelation::getTagId).distinct().collect(Collectors.toList())))
                .stream().collect(Collectors.toMap(WrongQuestionTag::getId, WrongQuestionTag::getTagName));
        Map<Long, List<String>> result = new HashMap<>();
        for (WrongQuestionTagRelation relation : relations) {
            String tagName = tagNameMap.get(relation.getTagId());
            if (tagName != null) {
                result.computeIfAbsent(relation.getWrongQuestionId(), key -> new ArrayList<>()).add(tagName);
            }
        }
        return result;
    }

    private void rewriteTags(Long wrongQuestionId, Long userId, List<String> tagNames, LocalDateTime now) {
        wrongQuestionTagRelationMapper.delete(new LambdaQueryWrapper<WrongQuestionTagRelation>()
                .eq(WrongQuestionTagRelation::getWrongQuestionId, wrongQuestionId)
                .eq(WrongQuestionTagRelation::getUserId, userId));
        for (String tagName : tagNames) {
            WrongQuestionTag tag = findOrCreateTag(userId, tagName, now);
            WrongQuestionTagRelation relation = new WrongQuestionTagRelation();
            relation.setWrongQuestionId(wrongQuestionId);
            relation.setTagId(tag.getId());
            relation.setUserId(userId);
            relation.setCreateTime(now);
            wrongQuestionTagRelationMapper.insert(relation);
        }
    }

    private WrongQuestionTag findOrCreateTag(Long userId, String tagName, LocalDateTime now) {
        WrongQuestionTag tag = wrongQuestionTagMapper.selectOne(new LambdaQueryWrapper<WrongQuestionTag>()
                .eq(WrongQuestionTag::getUserId, userId)
                .eq(WrongQuestionTag::getTagName, tagName));
        if (tag != null) {
            return tag;
        }
        tag = new WrongQuestionTag();
        tag.setUserId(userId);
        tag.setTagName(tagName);
        tag.setCreateTime(now);
        tag.setUpdateTime(now);
        try {
            wrongQuestionTagMapper.insert(tag);
            return tag;
        } catch (DuplicateKeyException duplicateKeyException) {
            WrongQuestionTag concurrentTag = wrongQuestionTagMapper.selectOne(new LambdaQueryWrapper<WrongQuestionTag>()
                    .eq(WrongQuestionTag::getUserId, userId)
                    .eq(WrongQuestionTag::getTagName, tagName));
            if (concurrentTag == null) {
                throw duplicateKeyException;
            }
            return concurrentTag;
        }
    }

    private void validateKnowledgePoints(WrongQuestion wrongQuestion, List<Long> pointIds) {
        if (pointIds.isEmpty()) {
            return;
        }
        Set<Long> uniqueIds = new LinkedHashSet<>(pointIds);
        if (uniqueIds.size() != pointIds.size()) {
            throw new LogicException(ErrorCodeConstants.KNOWLEDGE_POINT_NOT_EXIST);
        }
        long count = knowledgePointService.count(new LambdaQueryWrapper<KnowledgePoint>()
                .in(KnowledgePoint::getId, uniqueIds)
                .eq(KnowledgePoint::getGrade, wrongQuestion.getGrade())
                .eq(KnowledgePoint::getSubject, wrongQuestion.getSubject())
                .eq(KnowledgePoint::getEnable, 1));
        if (count != uniqueIds.size()) {
            throw new LogicException(ErrorCodeConstants.KNOWLEDGE_POINT_NOT_EXIST);
        }
    }

    private boolean hasAnyChange(WrongQuestionBatchOrganizeReq request) {
        return request.getTextbookVersion() != null || request.getChapterName() != null
                || request.getTagNames() != null || request.getFavorite() != null
                || request.getPriorityLevel() != null || request.getKnowledgePointIds() != null
                || Boolean.TRUE.equals(request.getArchive());
    }

    private List<String> normalizeTagNames(Collection<String> values) {
        Set<String> result = new LinkedHashSet<>();
        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }
            String normalized = value.trim();
            if (normalized.length() > 30) {
                throw new LogicException(ErrorCodeConstants.UPDATE_WRONG_QUESTION_FAIL);
            }
            result.add(normalized);
        }
        return new ArrayList<>(result);
    }

    private String trimToEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
