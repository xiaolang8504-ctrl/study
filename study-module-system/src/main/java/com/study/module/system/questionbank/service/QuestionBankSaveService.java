package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.request.QuestionBankSaveReq;
import com.study.module.system.questionbank.entity.QuestionBank;

/**
 * 题库题目保存服务
 */
public interface QuestionBankSaveService extends IService<QuestionBank> {

    /**
     * 保存题库题目
     */
    void saveQuestionBank(QuestionBankSaveReq request);
}
