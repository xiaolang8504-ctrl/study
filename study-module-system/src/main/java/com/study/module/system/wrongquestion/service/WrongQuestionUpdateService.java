package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.dto.request.UpdateWrongQuestionImageReq;
import com.study.module.system.wrongquestion.dto.request.UpdateWrongQuestionReq;
import com.study.module.system.wrongquestion.entity.WrongQuestion;

/**
 * 初中生错题修改服务
 */
public interface WrongQuestionUpdateService extends IService<WrongQuestion> {

    /**
     * 错题修改
     */
    void updateWrongQuestion(UpdateWrongQuestionReq request);

    /**
     * 错图修改
     */
    void updateWrongQuestionImage(UpdateWrongQuestionImageReq request);
}
