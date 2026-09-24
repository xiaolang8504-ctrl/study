package com.study.module.system.questionbank.service;

import com.study.module.system.questionbank.dto.request.QuestionBankVersionRollbackReq;
import com.study.module.system.questionbank.dto.request.QuestionContentGovernanceReq;
import com.study.module.system.questionbank.dto.response.QuestionContentGovernanceResp;

import java.util.List;

/** 内容审核、授权与版本回退服务。 */
public interface QuestionContentGovernanceService {
    void governQuestionContent(QuestionContentGovernanceReq request);
    void rollbackQuestionBankVersion(QuestionBankVersionRollbackReq request);
    List<QuestionContentGovernanceResp> questionContentGovernanceList(Long questionId);
}
