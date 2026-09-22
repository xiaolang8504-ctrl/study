package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.entity.WrongQuestion;

/**
 * 错题状态流转服务
 */
public interface WrongQuestionStatusService extends IService<WrongQuestion> {

    /**
     * 更新错题状态
     */
    void updateWrongQuestionStatus(Long id, Integer status, String remark);
}
