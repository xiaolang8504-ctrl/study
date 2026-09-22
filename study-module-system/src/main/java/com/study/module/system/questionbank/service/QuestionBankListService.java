package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.questionbank.dto.request.QuestionBankPageListReq;
import com.study.module.system.questionbank.dto.response.QuestionBankPageListResp;
import com.study.module.system.questionbank.entity.QuestionBank;

/**
 * 题库题目列表服务
 */
public interface QuestionBankListService extends IService<QuestionBank> {

    /**
     * 分页查询题库题目
     */
    PageResult<QuestionBankPageListResp> questionBankPageList(QuestionBankPageListReq request);
}
