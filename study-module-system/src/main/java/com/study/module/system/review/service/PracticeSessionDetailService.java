package com.study.module.system.review.service;

import com.study.module.system.review.dto.response.PracticeSessionDetailResp;

/**
 * 专项练习详情服务
 */
public interface PracticeSessionDetailService {

    PracticeSessionDetailResp practiceSessionDetail(Long sessionId);

    /**
     * 查询用于打印或导出的练习详情，包含参考答案和解析。
     */
    PracticeSessionDetailResp practicePaperDetail(Long sessionId);
}
