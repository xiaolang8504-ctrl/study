package com.study.module.system.questionbank.convert;

import com.study.module.system.questionbank.dto.request.QuestionBankSaveReq;
import com.study.module.system.questionbank.dto.request.QuestionExperimentSaveReq;
import com.study.module.system.questionbank.dto.response.QuestionBankReviewHistoryResp;
import com.study.module.system.questionbank.dto.response.QuestionBankDetailResp;
import com.study.module.system.questionbank.dto.response.QuestionBankPageListResp;
import com.study.module.system.questionbank.dto.response.QuestionExperimentHistoryResp;
import com.study.module.system.questionbank.dto.response.QuestionExperimentResp;
import com.study.module.system.questionbank.dto.response.QuestionPracticeAppealListResp;
import com.study.module.system.questionbank.dto.response.QuestionPracticeHistoryPageListResp;
import com.study.module.system.questionbank.entity.QuestionBank;
import com.study.module.system.questionbank.entity.QuestionBankReviewLog;
import com.study.module.system.questionbank.entity.QuestionExperiment;
import com.study.module.system.questionbank.entity.QuestionExperimentHistory;
import com.study.module.system.questionbank.entity.QuestionPracticeAppeal;
import com.study.module.system.questionbank.entity.QuestionRecommendationLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 题库数据转换类
 */
@Mapper
public interface QuestionBankConvert {

    QuestionBankConvert INSTANCE = Mappers.getMapper(QuestionBankConvert.class);

    /**
     * 转换为题库题目响应
     */
    QuestionBankDetailResp toQuestionBankDetailResp(QuestionBank questionBank);

    /**
     * 转换为题库题目分页列表响应
     */
    List<QuestionBankPageListResp> toQuestionBankPageListResp(List<QuestionBank> questionBankList);

    /**
     * 将保存请求数据更新到题库题目实体
     */
    void updateQuestionBank(QuestionBankSaveReq request, @MappingTarget QuestionBank questionBank);

    /**
     * 将实验保存请求更新到实验实体。
     */
    void updateQuestionExperiment(QuestionExperimentSaveReq request, @MappingTarget QuestionExperiment experiment);

    /**
     * 转换为实验历史实体。
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "experimentId", ignore = true)
    @Mapping(target = "operatorId", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    QuestionExperimentHistory toQuestionExperimentHistory(QuestionExperiment experiment);

    /**
     * 转换为实验历史响应。
     */
    QuestionExperimentHistoryResp toQuestionExperimentHistoryResp(QuestionExperimentHistory history);

    /**
     * 转换为实验响应。
     */
    QuestionExperimentResp toQuestionExperimentResp(QuestionExperiment experiment);

    /**
     * 转换为练习历史分页响应。
     */
    QuestionPracticeHistoryPageListResp toQuestionPracticeHistoryPageListResp(QuestionRecommendationLog log);

    /**
     * 转换为练习申诉列表响应。
     */
    QuestionPracticeAppealListResp toQuestionPracticeAppealListResp(QuestionPracticeAppeal appeal);

    /**
     * 转换为题库审核历史响应。
     */
    QuestionBankReviewHistoryResp toQuestionBankReviewHistoryResp(QuestionBankReviewLog reviewLog);

}
