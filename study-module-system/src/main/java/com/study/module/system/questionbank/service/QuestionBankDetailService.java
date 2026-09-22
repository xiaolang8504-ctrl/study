package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.response.QuestionBankDetailResp;
import com.study.module.system.questionbank.entity.QuestionBank;

/**
 * 题库题目详情服务
 */
public interface QuestionBankDetailService extends IService<QuestionBank> {

    /**
     * 查询题库题目详情
     */
    QuestionBankDetailResp questionBankDetail(Long id);
}
