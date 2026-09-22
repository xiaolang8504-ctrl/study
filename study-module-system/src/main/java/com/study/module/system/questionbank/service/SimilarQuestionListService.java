package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.request.SimilarQuestionListReq;
import com.study.module.system.questionbank.dto.response.SimilarQuestionListResp;
import com.study.module.system.questionbank.entity.QuestionRecommendationLog;

import java.util.List;

/**
 * 相似题列表服务
 */
public interface SimilarQuestionListService extends IService<QuestionRecommendationLog> {

    /**
     * 查询推荐的相似题列表
     */
    List<SimilarQuestionListResp> similarQuestionList(SimilarQuestionListReq request);
}
