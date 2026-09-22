package com.study.module.system.questionbank.service;

import com.study.module.system.questionbank.dto.request.QuestionBankImportReq;
import com.study.module.system.questionbank.dto.response.QuestionBankImportResp;

/**
 * A4文件识别导入题库服务。
 */
public interface QuestionBankImportService {
    QuestionBankImportResp importQuestionBankFile(QuestionBankImportReq request);
}
