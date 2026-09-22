package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;

import java.util.List;

/**
 * 初中生错题删除服务
 */
public interface WrongQuestionDeleteService extends IService<WrongQuestion> {

    /**
     * 错题删除
     */
    void deleteWrongQuestion(Long id);

    /**
     * 错题批量删除
     */
    void batchDeleteWrongQuestion(List<Long> ids);
}
