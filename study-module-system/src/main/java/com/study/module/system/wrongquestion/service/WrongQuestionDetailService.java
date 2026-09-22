package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionDetailResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;

/**
 * 初中生错题详情服务
 */
public interface WrongQuestionDetailService extends IService<WrongQuestion> {

    /**
     * 错题详情
     */
    WrongQuestionDetailResp wrongQuestionDetail(Long id);
}
