package com.study.module.system.questionbank.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.module.system.questionbank.dto.request.QuestionBankReviewReq;
import com.study.module.system.questionbank.dto.request.QuestionBankSaveReq;
import com.study.module.system.questionbank.dto.response.QuestionBankReviewHistoryResp;
import com.study.module.system.questionbank.dto.response.QuestionBankVersionListResp;
import com.study.module.system.questionbank.convert.QuestionBankConvert;
import com.study.module.system.questionbank.entity.QuestionBankReviewLog;
import com.study.module.system.questionbank.entity.QuestionBankVersion;
import com.study.module.system.questionbank.service.QuestionBankHistoryService;
import com.study.module.system.questionbank.service.QuestionBankReviewLogService;
import com.study.module.system.questionbank.service.QuestionBankVersionService;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.service.UserService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 题目版本与审核历史服务实现。
 */
@Service
public class QuestionBankHistoryServiceImpl implements QuestionBankHistoryService {

    @Autowired
    QuestionBankVersionService questionBankVersionService;

    @Autowired
    QuestionBankReviewLogService questionBankReviewLogService;

    @Autowired
    UserService userService;

    /**
     * 创建或保存相关业务数据。
     */
    @Override
    public void createVersion(Long questionId, QuestionBankSaveReq request, String operationType) {
        QuestionBankVersion latest = questionBankVersionService.getOne(
                new LambdaQueryWrapper<QuestionBankVersion>()
                        .eq(QuestionBankVersion::getQuestionId, questionId)
                        .orderByDesc(QuestionBankVersion::getVersionNo).last("LIMIT 1"));
        QuestionBankSaveReq snapshot = JSON.parseObject(JSON.toJSONString(request), QuestionBankSaveReq.class);
        snapshot.setId(questionId);
        QuestionBankVersion version = new QuestionBankVersion();
        version.setQuestionId(questionId);
        version.setVersionNo(latest == null ? 1 : latest.getVersionNo() + 1);
        version.setOperationType(operationType);
        version.setSnapshotJson(JSON.toJSONString(snapshot));
        version.setOperatorId(AccountUtils.getUserId());
        version.setCreateTime(LocalDateTime.now());
        questionBankVersionService.save(version);
    }

    /**
     * 创建或保存复习。
     */
    @Override
    public void createReviewLog(QuestionBankReviewReq request, Long reviewerId) {
        QuestionBankReviewLog log = new QuestionBankReviewLog();
        log.setQuestionId(request.getId());
        log.setReviewStatus(request.getReviewStatus());
        log.setReviewRemark(request.getReviewRemark());
        log.setReviewerId(reviewerId);
        log.setReviewTime(LocalDateTime.now());
        questionBankReviewLogService.save(log);
    }

    /**
     * 执行 questionBankVersionList 业务处理。
     */
    @Override
    public List<QuestionBankVersionListResp> questionBankVersionList(Long questionId) {
        return questionBankVersionService.list(new LambdaQueryWrapper<QuestionBankVersion>()
                .eq(QuestionBankVersion::getQuestionId, questionId)
                .orderByDesc(QuestionBankVersion::getVersionNo)).stream().map(version -> {
            QuestionBankSaveReq snapshot = JSON.parseObject(version.getSnapshotJson(), QuestionBankSaveReq.class);
            QuestionBankVersionListResp response = new QuestionBankVersionListResp();
            response.setId(version.getId());
            response.setVersionNo(version.getVersionNo());
            response.setOperationType(version.getOperationType());
            response.setOperatorId(version.getOperatorId());
            response.setCreateTime(version.getCreateTime());
            response.setQuestionTitle(snapshot.getQuestionTitle());
            response.setQuestionContent(snapshot.getQuestionContent());
            response.setOptionsJson(snapshot.getOptionsJson());
            response.setCorrectAnswer(snapshot.getCorrectAnswer());
            response.setAnalysis(snapshot.getAnalysis());
            response.setKnowledgePointIds(snapshot.getKnowledgePointIds());
            return response;
        }).collect(Collectors.toList());
    }

    /**
     * 查询题库历史记录。
     */
    @Override
    public List<QuestionBankReviewHistoryResp> questionBankReviewHistory(Long questionId) {
        List<QuestionBankReviewLog> logs = questionBankReviewLogService.list(
                new LambdaQueryWrapper<QuestionBankReviewLog>()
                .eq(QuestionBankReviewLog::getQuestionId, questionId)
                .orderByDesc(QuestionBankReviewLog::getReviewTime, QuestionBankReviewLog::getId));
        List<Long> reviewerIds = logs.stream().map(QuestionBankReviewLog::getReviewerId)
                .filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = reviewerIds.isEmpty() ? Collections.emptyMap()
                : userService.listByIds(reviewerIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        return logs.stream().map(log -> {
                    QuestionBankReviewHistoryResp response = QuestionBankConvert.INSTANCE.toQuestionBankReviewHistoryResp(log);
                    User reviewer = userMap.get(log.getReviewerId());
                    if (reviewer != null) {
                        response.setReviewerName(org.springframework.util.StringUtils.hasText(reviewer.getRealName())
                                ? reviewer.getRealName() : reviewer.getUserName());
                    }
                    return response;
                }).collect(Collectors.toList());
    }
}
