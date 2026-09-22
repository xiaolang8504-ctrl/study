package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionKnowledgePointBindReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionKnowledgePointStatisticsReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionKnowledgePointStatisticsResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;

import java.util.List;

/**
 * 错题知识点绑定服务
 */
public interface WrongQuestionKnowledgePointBindService extends IService<WrongQuestion> {

    /**
     * 绑定错题知识点
     */
    void bindWrongQuestionKnowledgePoint(WrongQuestionKnowledgePointBindReq request);

    /**
     * 错题知识点统计
     */
    List<WrongQuestionKnowledgePointStatisticsResp> wrongQuestionKnowledgePointStatistics(
            WrongQuestionKnowledgePointStatisticsReq request);
}
