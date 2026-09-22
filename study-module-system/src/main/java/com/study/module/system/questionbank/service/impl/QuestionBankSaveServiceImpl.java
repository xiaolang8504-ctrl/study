package com.study.module.system.questionbank.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.questionbank.dto.request.QuestionBankSaveReq;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.mapper.QuestionBankMapper;
import com.study.module.system.questionbank.service.QuestionBankSaveService;
import com.study.module.system.questionbank.service.QuestionBankDuplicateService;
import com.study.module.system.questionbank.service.QuestionBankService;
import com.study.module.system.questionbank.dto.request.QuestionBankDuplicateCheckReq;
import com.study.module.system.questionbank.service.QuestionBankImageService;
import com.study.module.system.questionbank.service.QuestionKnowledgePointService;
import com.study.module.system.questionbank.service.QuestionBankHistoryService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import com.study.module.system.questionbank.convert.QuestionBankConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * 题库题目保存服务实现
 */
@Service
public class QuestionBankSaveServiceImpl extends ServiceImpl<QuestionBankMapper, QuestionBank>
        implements QuestionBankSaveService {

    @Autowired
    QuestionKnowledgePointService questionKnowledgePointService;

    @Autowired
    QuestionBankImageService questionBankImageService;

    @Autowired
    QuestionBankDuplicateService questionBankDuplicateService;

    @Autowired
    QuestionBankService questionBankService;

    @Autowired
    QuestionBankHistoryService questionBankHistoryService;

    /**
     * 保存题库题目
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestionBank(QuestionBankSaveReq request) {
        validateOptionsJson(request.getOptionsJson());
        QuestionBank entity = request.getId() == null ? new QuestionBank() : getById(request.getId());
        if (entity == null) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_NOT_EXIST);
        }
        QuestionBankConvert.INSTANCE.updateQuestionBank(request, entity);
        questionBankService.fillDictNames(entity);
        LocalDateTime now = LocalDateTime.now();
        entity.setDifficulty(entity.getDifficulty() == null ? 3 : entity.getDifficulty());
        entity.setContentFormat(StringUtils.hasText(entity.getContentFormat()) ? entity.getContentFormat() : "TEXT");
        entity.setQuestionHash(questionHash(entity.getGrade(), entity.getSubject(), entity.getQuestionContent()));
        QuestionBankDuplicateCheckReq duplicateRequest = new QuestionBankDuplicateCheckReq();
        duplicateRequest.setId(request.getId());
        duplicateRequest.setGrade(entity.getGrade());
        duplicateRequest.setSubject(entity.getSubject());
        duplicateRequest.setQuestionContent(entity.getQuestionContent());
        if (!Boolean.TRUE.equals(request.getDuplicateConfirmed())
                && !questionBankDuplicateService.duplicateQuestionList(duplicateRequest).isEmpty()) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_DUPLICATE);
        }
        entity.setEnable(entity.getEnable() == null ? 1 : entity.getEnable());
        entity.setReviewStatus(0);
        entity.setReviewRemark(null);
        entity.setReviewerId(null);
        entity.setReviewTime(null);
        entity.setUpdateTime(now);
        if (request.getId() == null) {
            entity.setCreateId(AccountUtils.getUserId());
            entity.setCreateTime(now);
        }
        if (!saveOrUpdate(entity)) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_SAVE_FAIL);
        }
        questionKnowledgePointService.rewrite(entity.getId(), request.getKnowledgePointIds());
        questionBankImageService.rewrite(entity.getId(), request.getImages(), request.getImageUrls());
        questionBankHistoryService.createVersion(entity.getId(), request,
                request.getId() == null ? "CREATE" : "UPDATE");
    }

    /**
     * 校验业务数据。
     */
    private void validateOptionsJson(String optionsJson) {
        if (!StringUtils.hasText(optionsJson)) {
            return;
        }
        try {
            JSONObject options = JSON.parseObject(optionsJson);
            if (options == null || options.isEmpty()) {
                throw new LogicException(ErrorCodeConstants.QUESTION_BANK_OPTIONS_INVALID);
            }
        } catch (LogicException e) {
            throw e;
        } catch (Exception e) {
            throw new LogicException(ErrorCodeConstants.QUESTION_BANK_OPTIONS_INVALID);
        }
    }

    /**
     * 生成用于重复题检测的题干摘要
     */
    private String questionHash(String grade, String subject, String content) {
        String source = String.join("|", grade == null ? "" : grade, subject == null ? "" : subject,
                content == null ? "" : content.replaceAll("[\\s，。]", "").toLowerCase());
        return DigestUtils.md5DigestAsHex(source.getBytes(StandardCharsets.UTF_8));
    }
}
