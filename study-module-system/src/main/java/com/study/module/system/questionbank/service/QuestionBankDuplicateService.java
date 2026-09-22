package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.request.QuestionBankDuplicateCheckReq;
import com.study.module.system.questionbank.dto.request.QuestionBankDuplicateCleanReq;
import com.study.module.system.questionbank.dto.request.QuestionBankDuplicateHistoryReq;
import com.study.module.system.questionbank.dto.response.QuestionBankDuplicatePairResp;
import com.study.module.system.questionbank.dto.response.QuestionBankDuplicateResp;
import com.study.module.system.questionbank.entity.QuestionBank;

import java.util.List;

/**
 * 题库重复题检测与清理服务
 */
public interface QuestionBankDuplicateService extends IService<QuestionBank> {

    List<QuestionBankDuplicateResp> duplicateQuestionList(QuestionBankDuplicateCheckReq request);

    List<QuestionBankDuplicatePairResp> duplicateQuestionHistory(QuestionBankDuplicateHistoryReq request);

    void cleanDuplicateQuestion(QuestionBankDuplicateCleanReq request);

    /**
     * 合并重复题目。
     */
    void mergeDuplicateQuestion(QuestionBankDuplicateCleanReq request);
}
