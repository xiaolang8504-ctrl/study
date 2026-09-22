package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.request.QuestionReportHandleReq;
import com.study.module.system.questionbank.entity.QuestionReport;

/**
 * 题目举报处理服务
 */
public interface QuestionReportHandleService extends IService<QuestionReport> {

    /**
     * 处理题目举报
     */
    void handleQuestionReport(QuestionReportHandleReq request);
}
