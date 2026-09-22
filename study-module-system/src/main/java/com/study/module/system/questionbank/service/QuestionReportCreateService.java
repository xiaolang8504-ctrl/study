package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.request.QuestionReportCreateReq;
import com.study.module.system.questionbank.entity.QuestionReport;

/**
 * 题目举报新增服务
 */
public interface QuestionReportCreateService extends IService<QuestionReport> {

    /**
     * 新增题目举报
     */
    void reportQuestion(QuestionReportCreateReq request);
}
