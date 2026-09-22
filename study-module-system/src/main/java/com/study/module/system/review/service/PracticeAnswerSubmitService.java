package com.study.module.system.review.service;

import com.study.module.system.review.dto.request.PracticeAnswerSubmitReq;
import com.study.module.system.review.dto.request.PracticeAnswerBatchSubmitReq;
import com.study.module.system.review.dto.request.PracticeAnswerDraftSaveReq;
import com.study.module.system.review.dto.response.PracticeAnswerSubmitResp;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;

/**
 * 专项练习作答服务
 */
public interface PracticeAnswerSubmitService {

    PracticeAnswerSubmitResp submitPracticeAnswer(PracticeAnswerSubmitReq request);

    /**
     * 批量提交专项练习作答
     */
    PracticeSessionDetailResp batchSubmitPracticeAnswer(PracticeAnswerBatchSubmitReq request);

    /**
     * 保存专项练习草稿
     */
    PracticeSessionDetailResp savePracticeAnswerDraft(PracticeAnswerDraftSaveReq request);
}
