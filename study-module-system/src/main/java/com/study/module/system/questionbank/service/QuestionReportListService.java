package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.questionbank.dto.request.QuestionReportPageListReq;
import com.study.module.system.questionbank.dto.response.QuestionReportPageListResp;
import com.study.module.system.questionbank.entity.QuestionReport;

/**
 * 题目举报列表服务
 */
public interface QuestionReportListService extends IService<QuestionReport> {

    /**
     * 分页查询题目举报
     */
    PageResult<QuestionReportPageListResp> questionReportPageList(QuestionReportPageListReq request);
}
