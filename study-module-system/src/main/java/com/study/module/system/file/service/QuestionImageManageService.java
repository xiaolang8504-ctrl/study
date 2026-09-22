package com.study.module.system.file.service;

import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.file.dto.request.QuestionImagePageListReq;
import com.study.module.system.file.dto.response.QuestionImageCleanResp;
import com.study.module.system.file.dto.response.QuestionImagePageListResp;

import java.util.List;

/**
 * 题目图片管理服务
 */
public interface QuestionImageManageService {

    PageResult<QuestionImagePageListResp> questionImagePageList(QuestionImagePageListReq request);

    QuestionImageCleanResp deleteOrphanQuestionImage(List<Integer> fileIds);

    QuestionImageCleanResp cleanOrphanQuestionImage(Integer retentionHours);
}
