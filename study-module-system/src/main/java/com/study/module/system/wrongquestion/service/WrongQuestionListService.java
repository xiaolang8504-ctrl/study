package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionPageListReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionPageListResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.common.core.domain.dto.PageResult;

/**
 * 初中生错题列表服务
 */
public interface WrongQuestionListService extends IService<WrongQuestion> {

    /**
     * 错题分页列表
     */
    PageResult<WrongQuestionPageListResp> wrongQuestionPageList(WrongQuestionPageListReq request);
}
