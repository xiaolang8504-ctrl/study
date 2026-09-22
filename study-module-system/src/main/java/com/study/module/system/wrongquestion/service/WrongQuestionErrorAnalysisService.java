package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionErrorAnalysisReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionErrorAnalysisStatisticsReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionErrorAnalysisStatisticsResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;

import java.util.List;

/**
 * 错题错因分析服务
 */
public interface WrongQuestionErrorAnalysisService extends IService<WrongQuestion> {

    /**
     * 更新错题错因分析
     */
    void updateWrongQuestionErrorAnalysis(WrongQuestionErrorAnalysisReq request);

    /**
     * 错因分析统计
     */
    List<WrongQuestionErrorAnalysisStatisticsResp> wrongQuestionErrorAnalysisStatistics(
            WrongQuestionErrorAnalysisStatisticsReq request);
}
