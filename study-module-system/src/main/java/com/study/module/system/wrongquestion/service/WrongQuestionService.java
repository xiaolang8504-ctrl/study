package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;

/**
 * 初中生错题归档服务
 */
public interface WrongQuestionService extends IService<WrongQuestion> {

    /**
     * 校验错题
     */
    WrongQuestion checkWrongQuestion(Long id);

    /**
     * 根据年级、科目和题目类型字典键值填充名称
     */
    void fillDictNames(WrongQuestion wrongQuestion);
}
