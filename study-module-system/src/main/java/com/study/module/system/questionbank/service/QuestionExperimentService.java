package com.study.module.system.questionbank.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.questionbank.dto.request.QuestionExperimentSaveReq;
import com.study.module.system.questionbank.dto.response.QuestionExperimentHistoryResp;
import com.study.module.system.questionbank.dto.response.QuestionExperimentResp;
import com.study.module.system.questionbank.entity.QuestionExperiment;

import java.util.List;

/**
 * 相似题 A/B 实验服务
 */
public interface QuestionExperimentService extends IService<QuestionExperiment> {
    QuestionExperimentResp questionExperimentDetail();
    void saveQuestionExperiment(QuestionExperimentSaveReq request);
    List<QuestionExperimentHistoryResp> questionExperimentHistory();
    QuestionExperiment currentRunningExperiment();
    String assignGroup(Long userId);
}
