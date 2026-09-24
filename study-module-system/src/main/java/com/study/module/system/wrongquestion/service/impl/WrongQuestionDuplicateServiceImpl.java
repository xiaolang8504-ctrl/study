package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.wrongquestion.constants.WrongQuestionStatus;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionDuplicateMergeReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionDuplicateResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionOccurrenceResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.entity.WrongQuestionDuplicateRelation;
import com.study.module.system.wrongquestion.entity.WrongQuestionOccurrence;
import com.study.module.system.wrongquestion.mapper.WrongQuestionDuplicateRelationMapper;
import com.study.module.system.wrongquestion.mapper.WrongQuestionOccurrenceMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionDuplicateService;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.module.system.wrongquestion.service.WrongQuestionTimelineService;
import com.study.module.system.wrongquestion.utils.WrongQuestionContentUtils;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** 错题重复检测、来源归并与撤销实现。 */
@Service
public class WrongQuestionDuplicateServiceImpl
        extends ServiceImpl<WrongQuestionDuplicateRelationMapper, WrongQuestionDuplicateRelation>
        implements WrongQuestionDuplicateService {

    private static final int SIMILARITY_THRESHOLD = 75;

    @Autowired
    WrongQuestionService wrongQuestionService;
    @Autowired
    WrongQuestionOccurrenceMapper wrongQuestionOccurrenceMapper;
    @Autowired
    WrongQuestionTimelineService wrongQuestionTimelineService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<WrongQuestionDuplicateResp> scanWrongQuestionDuplicate(Long wrongQuestionId) {
        WrongQuestion source = wrongQuestionService.checkWrongQuestion(wrongQuestionId);
        Long userId = AccountUtils.getUserId();
        if (source.getMergedToId() != null) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_DUPLICATE_RELATION_INVALID);
        }
        fillFingerprint(source);
        List<WrongQuestionDuplicateResp> responses = new ArrayList<>();
        List<WrongQuestionDuplicateRelation> mergedRelations = lambdaQuery()
                .eq(WrongQuestionDuplicateRelation::getUserId, userId)
                .eq(WrongQuestionDuplicateRelation::getStatus, "MERGED")
                .and(query -> query.eq(WrongQuestionDuplicateRelation::getLeftQuestionId, source.getId())
                        .or().eq(WrongQuestionDuplicateRelation::getRightQuestionId, source.getId()))
                .list();
        for (WrongQuestionDuplicateRelation relation : mergedRelations) {
            Long otherId = Objects.equals(source.getId(), relation.getLeftQuestionId())
                    ? relation.getRightQuestionId() : relation.getLeftQuestionId();
            WrongQuestion other = wrongQuestionService.getById(otherId);
            if (other != null) {
                responses.add(toResponse(relation, other));
            }
        }
        List<WrongQuestion> candidates = wrongQuestionService.lambdaQuery()
                .eq(WrongQuestion::getCreateId, userId)
                .isNull(WrongQuestion::getMergedToId)
                .ne(WrongQuestion::getId, source.getId())
                .eq(source.getSubject() != null, WrongQuestion::getSubject, source.getSubject())
                .orderByDesc(WrongQuestion::getId)
                .last("LIMIT 200")
                .list();
        String sourceText = WrongQuestionContentUtils.normalized(source.getQuestionTitle(), source.getQuestionContent());
        for (WrongQuestion candidate : candidates) {
            fillFingerprint(candidate);
            String candidateText = WrongQuestionContentUtils.normalized(candidate.getQuestionTitle(), candidate.getQuestionContent());
            int score = WrongQuestionContentUtils.similarity(sourceText, candidateText);
            boolean exact = source.getQuestionFingerprint() != null
                    && source.getQuestionFingerprint().equals(candidate.getQuestionFingerprint());
            if (!exact && score < SIMILARITY_THRESHOLD) {
                continue;
            }
            String matchType = exact
                    ? (differentCaptureSource(source, candidate) ? "SAME_QUESTION_DIFFERENT_SOURCE" : "EXACT")
                    : "SIMILAR";
            WrongQuestionDuplicateRelation relation = findOrCreateRelation(source, candidate, matchType, score, userId);
            responses.add(toResponse(relation, candidate));
        }
        return responses.stream()
                .sorted(Comparator.comparing(WrongQuestionDuplicateResp::getSimilarityScore).reversed()
                        .thenComparing(WrongQuestionDuplicateResp::getQuestionId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mergeWrongQuestionDuplicate(WrongQuestionDuplicateMergeReq request) {
        Long userId = AccountUtils.getUserId();
        WrongQuestionDuplicateRelation relation = lambdaQuery()
                .eq(WrongQuestionDuplicateRelation::getId, request.getRelationId())
                .eq(WrongQuestionDuplicateRelation::getUserId, userId)
                .in(WrongQuestionDuplicateRelation::getStatus, "CANDIDATE", "REVERSED").one();
        if (relation == null) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_DUPLICATE_RELATION_INVALID);
        }
        if ("SIMILAR".equals(relation.getMatchType())) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_SIMILAR_CANNOT_MERGE);
        }
        if (!Objects.equals(request.getKeepQuestionId(), relation.getLeftQuestionId())
                && !Objects.equals(request.getKeepQuestionId(), relation.getRightQuestionId())) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_DUPLICATE_RELATION_INVALID);
        }
        Long mergedId = Objects.equals(request.getKeepQuestionId(), relation.getLeftQuestionId())
                ? relation.getRightQuestionId() : relation.getLeftQuestionId();
        WrongQuestion kept = wrongQuestionService.checkWrongQuestion(request.getKeepQuestionId());
        WrongQuestion merged = wrongQuestionService.checkWrongQuestion(mergedId);
        if (kept.getMergedToId() != null || merged.getMergedToId() != null) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_DUPLICATE_RELATION_INVALID);
        }
        ensureOccurrence(kept);
        ensureOccurrence(merged);
        wrongQuestionOccurrenceMapper.update(null, new LambdaUpdateWrapper<WrongQuestionOccurrence>()
                .eq(WrongQuestionOccurrence::getCanonicalQuestionId, merged.getId())
                .set(WrongQuestionOccurrence::getCanonicalQuestionId, kept.getId()));
        int beforeStatus = merged.getStatus() == null ? WrongQuestionStatus.PENDING_CORRECTION : merged.getStatus();
        merged.setMergedToId(kept.getId());
        merged.setStatus(WrongQuestionStatus.ARCHIVED);
        merged.setUpdateTime(LocalDateTime.now());
        wrongQuestionService.updateById(merged);
        relation.setStatus("MERGED");
        relation.setKeptQuestionId(kept.getId());
        relation.setMergedQuestionId(merged.getId());
        relation.setMergedBeforeStatus(beforeStatus);
        relation.setUpdateTime(LocalDateTime.now());
        updateById(relation);
        wrongQuestionTimelineService.record(kept.getId(), "DUPLICATE_MERGED", "STUDENT",
                "已归并错题 #" + merged.getId() + " 的错误来源", userId);
        wrongQuestionTimelineService.record(merged.getId(), "DUPLICATE_MERGED", "STUDENT",
                "已归并至错题 #" + kept.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void undoWrongQuestionMerge(Long relationId) {
        Long userId = AccountUtils.getUserId();
        WrongQuestionDuplicateRelation relation = lambdaQuery()
                .eq(WrongQuestionDuplicateRelation::getId, relationId)
                .eq(WrongQuestionDuplicateRelation::getUserId, userId)
                .eq(WrongQuestionDuplicateRelation::getStatus, "MERGED").one();
        if (relation == null || relation.getMergedQuestionId() == null) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_DUPLICATE_RELATION_INVALID);
        }
        WrongQuestion merged = wrongQuestionService.checkWrongQuestion(relation.getMergedQuestionId());
        if (!Objects.equals(merged.getMergedToId(), relation.getKeptQuestionId())) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_DUPLICATE_RELATION_INVALID);
        }
        merged.setMergedToId(null);
        merged.setStatus(relation.getMergedBeforeStatus() == null
                ? WrongQuestionStatus.PENDING_CORRECTION : relation.getMergedBeforeStatus());
        merged.setUpdateTime(LocalDateTime.now());
        wrongQuestionService.updateById(merged);
        wrongQuestionOccurrenceMapper.update(null, new LambdaUpdateWrapper<WrongQuestionOccurrence>()
                .eq(WrongQuestionOccurrence::getOriginWrongQuestionId, merged.getId())
                .set(WrongQuestionOccurrence::getCanonicalQuestionId, merged.getId()));
        relation.setStatus("REVERSED");
        relation.setUpdateTime(LocalDateTime.now());
        updateById(relation);
        wrongQuestionTimelineService.record(merged.getId(), "DUPLICATE_MERGE_REVERSED", "STUDENT",
                "已撤销与错题 #" + relation.getKeptQuestionId() + " 的归并", userId);
    }

    @Override
    public List<WrongQuestionOccurrenceResp> wrongQuestionOccurrenceList(Long wrongQuestionId) {
        WrongQuestion question = wrongQuestionService.checkWrongQuestion(wrongQuestionId);
        Long canonicalId = question.getMergedToId() == null ? question.getId() : question.getMergedToId();
        ensureOccurrence(question);
        return wrongQuestionOccurrenceMapper.selectList(new LambdaQueryWrapper<WrongQuestionOccurrence>()
                        .eq(WrongQuestionOccurrence::getCanonicalQuestionId, canonicalId)
                        .orderByDesc(WrongQuestionOccurrence::getOccurredAt, WrongQuestionOccurrence::getId))
                .stream().map(item -> {
                    WrongQuestionOccurrenceResp response = new WrongQuestionOccurrenceResp();
                    BeanUtils.copyProperties(item, response);
                    return response;
                }).collect(Collectors.toList());
    }

    private WrongQuestionDuplicateRelation findOrCreateRelation(WrongQuestion source, WrongQuestion candidate,
                                                                 String matchType, int score, Long userId) {
        long left = Math.min(source.getId(), candidate.getId());
        long right = Math.max(source.getId(), candidate.getId());
        WrongQuestionDuplicateRelation relation = lambdaQuery()
                .eq(WrongQuestionDuplicateRelation::getUserId, userId)
                .eq(WrongQuestionDuplicateRelation::getLeftQuestionId, left)
                .eq(WrongQuestionDuplicateRelation::getRightQuestionId, right).one();
        if (relation != null) {
            if (!"MERGED".equals(relation.getStatus())) {
                relation.setMatchType(matchType);
                relation.setSimilarityScore(score);
                relation.setStatus("CANDIDATE");
                relation.setUpdateTime(LocalDateTime.now());
                updateById(relation);
            }
            return relation;
        }
        relation = new WrongQuestionDuplicateRelation();
        relation.setUserId(userId);
        relation.setLeftQuestionId(left);
        relation.setRightQuestionId(right);
        relation.setMatchType(matchType);
        relation.setSimilarityScore(score);
        relation.setStatus("CANDIDATE");
        relation.setCreateId(userId);
        relation.setCreateTime(LocalDateTime.now());
        relation.setUpdateTime(relation.getCreateTime());
        try {
            save(relation);
            return relation;
        } catch (DuplicateKeyException duplicate) {
            return lambdaQuery().eq(WrongQuestionDuplicateRelation::getUserId, userId)
                    .eq(WrongQuestionDuplicateRelation::getLeftQuestionId, left)
                    .eq(WrongQuestionDuplicateRelation::getRightQuestionId, right).one();
        }
    }

    private WrongQuestionDuplicateResp toResponse(WrongQuestionDuplicateRelation relation,
                                                   WrongQuestion candidate) {
        WrongQuestionDuplicateResp response = new WrongQuestionDuplicateResp();
        response.setRelationId(relation.getId());
        response.setQuestionId(candidate.getId());
        response.setQuestionTitle(candidate.getQuestionTitle());
        response.setMatchType(relation.getMatchType());
        response.setSimilarityScore(relation.getSimilarityScore());
        response.setStatus(relation.getStatus());
        return response;
    }

    private void fillFingerprint(WrongQuestion question) {
        String fingerprint = WrongQuestionContentUtils.fingerprint(
                question.getQuestionTitle(), question.getQuestionContent(), question.getOptionsJson());
        if (!Objects.equals(question.getQuestionFingerprint(), fingerprint)) {
            question.setQuestionFingerprint(fingerprint);
            wrongQuestionService.lambdaUpdate().eq(WrongQuestion::getId, question.getId())
                    .set(WrongQuestion::getQuestionFingerprint, fingerprint).update();
        }
    }

    private boolean differentCaptureSource(WrongQuestion left, WrongQuestion right) {
        return left.getCaptureTaskId() != null && right.getCaptureTaskId() != null
                && !Objects.equals(left.getCaptureTaskId(), right.getCaptureTaskId());
    }

    private void ensureOccurrence(WrongQuestion question) {
        Long count = wrongQuestionOccurrenceMapper.selectCount(new LambdaQueryWrapper<WrongQuestionOccurrence>()
                .eq(WrongQuestionOccurrence::getOriginWrongQuestionId, question.getId()));
        if (count != null && count > 0) {
            return;
        }
        WrongQuestionOccurrence occurrence = new WrongQuestionOccurrence();
        occurrence.setCanonicalQuestionId(question.getMergedToId() == null ? question.getId() : question.getMergedToId());
        occurrence.setOriginWrongQuestionId(question.getId());
        occurrence.setUserId(question.getCreateId());
        occurrence.setWrongAnswer(question.getWrongAnswer());
        occurrence.setSource(question.getSource());
        occurrence.setSourceName(question.getSourceName());
        occurrence.setCaptureTaskId(question.getCaptureTaskId());
        occurrence.setCapturePageId(question.getCapturePageId());
        occurrence.setCaptureRegionId(question.getCaptureRegionId());
        occurrence.setCaptureSourcePageNo(question.getCaptureSourcePageNo());
        occurrence.setLeftPosition(question.getCaptureLeftPosition());
        occurrence.setTopPosition(question.getCaptureTopPosition());
        occurrence.setWidth(question.getCaptureWidth());
        occurrence.setHeight(question.getCaptureHeight());
        occurrence.setOccurredAt(question.getCreateTime());
        occurrence.setCreateTime(LocalDateTime.now());
        try {
            wrongQuestionOccurrenceMapper.insert(occurrence);
        } catch (DuplicateKeyException ignored) {
            // 并发首次访问由唯一键保证每个原始错题只有一个来源快照。
        }
    }
}
