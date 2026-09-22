package com.study.module.system.review.service;

import com.study.module.system.review.dto.request.PracticeSessionCreateReq;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import com.study.module.system.review.dto.response.PracticeSessionPreviewResp;

/**
 * 创建专项练习服务
 */
public interface PracticeSessionCreateService {

    PracticeSessionPreviewResp previewPracticeSession(PracticeSessionCreateReq request);

    PracticeSessionDetailResp createPracticeSession(PracticeSessionCreateReq request);
}
