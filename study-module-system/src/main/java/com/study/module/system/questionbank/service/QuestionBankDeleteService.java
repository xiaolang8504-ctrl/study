package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.entity.QuestionBank;

/**
 * 题库题目删除服务
 */
public interface QuestionBankDeleteService extends IService<QuestionBank> {

    /**
     * 删除题库题目
     */
    void deleteQuestionBank(Long id);
}
