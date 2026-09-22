package com.study.module.system.questionbank.service;
import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.questionbank.dto.request.QuestionPracticeHistoryPageListReq;
import com.study.module.system.questionbank.dto.response.QuestionPracticeHistoryPageListResp;
/**
 * 相似题练习历史列表服务。
 */
public interface QuestionPracticeHistoryListService {
    PageResult<QuestionPracticeHistoryPageListResp> questionPracticeHistoryPageList(QuestionPracticeHistoryPageListReq request);
}
