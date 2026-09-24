package com.study.module.system.wrongquestion.service;

import com.study.module.system.wrongquestion.dto.request.WrongQuestionBatchOrganizeReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionSavedFilterSaveReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionSavedFilterResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionTagResp;

import java.util.List;
import java.util.Map;

/** 错题教材、标签和常用筛选的整理服务。 */
public interface WrongQuestionOrganizeService {

    void batchOrganizeWrongQuestion(WrongQuestionBatchOrganizeReq request);

    List<WrongQuestionTagResp> wrongQuestionTagList();

    List<WrongQuestionSavedFilterResp> savedWrongQuestionFilterList();

    Long saveWrongQuestionFilter(WrongQuestionSavedFilterSaveReq request);

    void deleteWrongQuestionFilter(Long id);

    Map<Long, List<String>> resolveTagNames(List<Long> wrongQuestionIds);
}
