package com.study.module.system.review.service;

import com.study.module.system.review.dto.request.PracticePaperAnswerFillReq;
import com.study.module.system.review.dto.response.PracticePaperFillDetailResp;

/**
 * 纸面练习卷逐题回填服务。
 */
public interface PracticePaperFillService {

    PracticePaperFillDetailResp practicePaperFillDetail(String paperCode);

    PracticePaperFillDetailResp submitPracticePaperAnswerFill(PracticePaperAnswerFillReq request);
}
