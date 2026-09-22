package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.dto.request.CreateWrongQuestionReq;
import com.study.module.system.wrongquestion.entity.WrongQuestion;

/**
 * 初中生错题录入服务
 */
public interface WrongQuestionCreateService extends IService<WrongQuestion> {

    /**
     * 错题录入
     */
    void createWrongQuestion(CreateWrongQuestionReq request);
}
