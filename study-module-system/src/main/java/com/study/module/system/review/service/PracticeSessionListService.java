package com.study.module.system.review.service;

import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.review.dto.request.PracticeSessionPageListReq;
import com.study.module.system.review.dto.response.PracticeSessionPageListResp;

/**
 * 专项练习历史列表服务
 */
public interface PracticeSessionListService {

    PageResult<PracticeSessionPageListResp> practiceSessionPageList(PracticeSessionPageListReq request);
}
