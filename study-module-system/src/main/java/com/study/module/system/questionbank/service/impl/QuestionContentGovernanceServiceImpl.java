package com.study.module.system.questionbank.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.file.service.FileService;
import com.study.module.system.questionbank.convert.QuestionBankConvert;
import com.study.module.system.questionbank.dto.request.QuestionBankSaveReq;
import com.study.module.system.questionbank.dto.request.QuestionBankVersionRollbackReq;
import com.study.module.system.questionbank.dto.request.QuestionContentGovernanceReq;
import com.study.module.system.questionbank.dto.response.QuestionContentGovernanceResp;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.entity.QuestionBankVersion;
import com.study.module.system.questionbank.entity.QuestionContentGovernance;
import com.study.module.system.questionbank.mapper.QuestionContentGovernanceMapper;
import com.study.module.system.questionbank.service.QuestionBankHistoryService;
import com.study.module.system.questionbank.service.QuestionBankImageService;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.study.module.system.questionbank.service.QuestionBankVersionService;
import com.study.module.system.questionbank.service.QuestionContentGovernanceService;
import com.study.module.system.questionbank.service.QuestionKnowledgePointService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目内容治理实现。
 *
 * 下架只改变后续投放资格：推荐 SQL 仍以 enable、审核状态和授权到期日过滤；
 * 已创建的练习会话与作答快照从不在这里删除或改写。
 */
@Service
public class QuestionContentGovernanceServiceImpl implements QuestionContentGovernanceService {

    private static final String QUESTION_BANK_UPLOAD_TYPE = "questionBank";
    private static final Set<String> ISSUE_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "STEM_ERROR", "ANSWER_ERROR", "ANALYSIS_ERROR", "OUT_OF_SYLLABUS", "DUPLICATE",
            "INFRINGEMENT", "LICENSE_EXPIRED", "CONTENT_ERROR")));
    private static final Set<String> ACTIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "DOWN", "RESTORE", "LICENSE_UPDATE", "RECORD_PROOF")));

    @Autowired
    QuestionBankService questionBankService;

    @Autowired
    QuestionBankVersionService questionBankVersionService;

    @Autowired
    QuestionBankHistoryService questionBankHistoryService;

    @Autowired
    QuestionKnowledgePointService questionKnowledgePointService;

    @Autowired
    QuestionBankImageService questionBankImageService;

    @Autowired
    FileService fileService;

    @Autowired
    QuestionContentGovernanceMapper questionContentGovernanceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void governQuestionContent(QuestionContentGovernanceReq request) {
        String action = normalize(request.getAction());
        String issueType = normalize(request.getIssueType());
        if (!ACTIONS.contains(action)) {
            throw new LogicException(ErrorCodeConstants.QUESTION_CONTENT_ACTION_INVALID);
        }
        if (!ISSUE_TYPES.contains(issueType)) {
            throw new LogicException(ErrorCodeConstants.QUESTION_CONTENT_ISSUE_TYPE_INVALID);
        }
        QuestionBank question = requireQuestion(request.getQuestionId());
        String proofFileIds = validateProofFiles(request.getProofFileIds());
        LocalDate expireAt = request.getExpireAt();
        if ("DOWN".equals(action)) {
            question.setEnable(0);
        } else if ("RESTORE".equals(action)) {
            if (!Integer.valueOf(1).equals(question.getReviewStatus())
                    || (question.getExpireAt() != null && question.getExpireAt().isBefore(LocalDate.now()))) {
                throw new LogicException(ErrorCodeConstants.QUESTION_CONTENT_RESTORE_INVALID);
            }
            question.setEnable(1);
        } else if ("LICENSE_UPDATE".equals(action)) {
            question.setLicenseVersion(request.getLicenseVersion());
            question.setExpireAt(expireAt);
            if (expireAt != null && expireAt.isBefore(LocalDate.now())) {
                question.setEnable(0);
            }
        }
        question.setUpdateTime(LocalDateTime.now());
        questionBankService.updateById(question);
        saveGovernance(question.getId(), action, issueType, request.getHandleRemark(), proofFileIds,
                request.getLicenseVersion(), expireAt, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rollbackQuestionBankVersion(QuestionBankVersionRollbackReq request) {
        QuestionBank question = requireQuestion(request.getQuestionId());
        QuestionBankVersion version = questionBankVersionService.getOne(new LambdaQueryWrapper<QuestionBankVersion>()
                .eq(QuestionBankVersion::getQuestionId, request.getQuestionId())
                .eq(QuestionBankVersion::getVersionNo, request.getVersionNo()).last("LIMIT 1"));
        if (version == null) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_VERSION_NOT_EXIST);
        }
        QuestionBankSaveReq snapshot = JSON.parseObject(version.getSnapshotJson(), QuestionBankSaveReq.class);
        if (snapshot == null) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_VERSION_NOT_EXIST);
        }
        snapshot.setId(question.getId());
        QuestionBankConvert.INSTANCE.updateQuestionBank(snapshot, question);
        questionBankService.fillDictNames(question);
        // 回退后的内容必须重新审核才可恢复投放，且默认不在推荐池中出现。
        question.setReviewStatus(0);
        question.setReviewRemark("已从版本 V" + request.getVersionNo() + " 回退，等待重新审核");
        question.setReviewerId(null);
        question.setReviewTime(null);
        question.setEnable(0);
        question.setUpdateTime(LocalDateTime.now());
        questionBankService.updateById(question);
        // 旧版本不一定含关联快照；缺失字段时保留当前关联而不是误删。
        if (snapshot.getKnowledgePointIds() != null) {
            questionKnowledgePointService.rewrite(question.getId(), snapshot.getKnowledgePointIds());
        }
        if (snapshot.getImages() != null || StringUtils.hasText(snapshot.getImageUrls())) {
            questionBankImageService.rewrite(question.getId(), snapshot.getImages(), snapshot.getImageUrls());
        }
        questionBankHistoryService.createVersion(question.getId(), snapshot, "ROLLBACK");
        saveGovernance(question.getId(), "ROLLBACK", "CONTENT_ERROR", request.getHandleRemark(), null,
                question.getLicenseVersion(), question.getExpireAt(), request.getVersionNo());
    }

    @Override
    public List<QuestionContentGovernanceResp> questionContentGovernanceList(Long questionId) {
        requireQuestion(questionId);
        return questionContentGovernanceMapper.selectList(new LambdaQueryWrapper<QuestionContentGovernance>()
                        .eq(QuestionContentGovernance::getQuestionId, questionId)
                        .orderByDesc(QuestionContentGovernance::getId)).stream()
                .map(item -> {
                    QuestionContentGovernanceResp response = new QuestionContentGovernanceResp();
                    BeanUtils.copyProperties(item, response);
                    return response;
                }).collect(Collectors.toList());
    }

    private QuestionBank requireQuestion(Long questionId) {
        QuestionBank question = questionBankService.getById(questionId);
        if (question == null) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_NOT_EXIST);
        }
        return question;
    }

    private String validateProofFiles(List<Integer> proofFileIds) {
        if (proofFileIds == null || proofFileIds.isEmpty()) {
            return null;
        }
        for (Integer fileId : proofFileIds) {
            fileService.checkCurrentUserFile(fileId, QUESTION_BANK_UPLOAD_TYPE);
        }
        return proofFileIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private void saveGovernance(Long questionId, String action, String issueType, String remark, String proofFileIds,
                                String licenseVersion, LocalDate expireAt, Integer targetVersionNo) {
        QuestionContentGovernance record = new QuestionContentGovernance();
        record.setQuestionId(questionId);
        record.setAction(action);
        record.setIssueType(issueType);
        record.setHandleRemark(remark);
        record.setProofFileIds(proofFileIds);
        record.setLicenseVersion(licenseVersion);
        record.setExpireAt(expireAt);
        record.setTargetVersionNo(targetVersionNo);
        record.setOperatorId(AccountUtils.getUserId());
        record.setCreateTime(LocalDateTime.now());
        questionContentGovernanceMapper.insert(record);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }
}
