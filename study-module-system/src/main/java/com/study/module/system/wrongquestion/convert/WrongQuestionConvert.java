package com.study.module.system.wrongquestion.convert;

import com.study.module.system.wrongquestion.dto.request.CreateWrongQuestionReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureTaskCreateReq;
import com.study.module.system.wrongquestion.dto.request.UpdateWrongQuestionReq;
import com.study.module.system.wrongquestion.dto.response.QuestionCapturePageResp;
import com.study.module.system.wrongquestion.dto.response.QuestionCaptureRegionResp;
import com.study.module.system.wrongquestion.dto.response.QuestionCaptureTaskPageListResp;
import com.study.module.system.wrongquestion.dto.response.QuestionCaptureTaskResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionCorrectionRecordResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionDetailResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionPageListResp;
import com.study.module.system.wrongquestion.entity.QuestionCapturePage;
import com.study.module.system.wrongquestion.entity.QuestionCaptureRegion;
import com.study.module.system.wrongquestion.entity.QuestionCaptureTask;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.entity.WrongQuestionCorrectionRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import java.util.List;

/**
 * 初中生错题归档转化类
 */
@Mapper
public interface WrongQuestionConvert {

    WrongQuestionConvert INSTANCE = Mappers.getMapper(WrongQuestionConvert.class);

    /**
     * 转化为错题分页列表响应类
     */
    List<WrongQuestionPageListResp> toWrongQuestionPageListResp(List<WrongQuestion> wrongQuestionList);

    /**
     * 转化为错题详情响应类
     */
    WrongQuestionDetailResp toWrongQuestionDetailResp(WrongQuestion wrongQuestion);

    /**
     * 转化为错题实体
     */
    WrongQuestion toWrongQuestion(CreateWrongQuestionReq request);

    /**
     * 转化为错题实体
     */
    WrongQuestion toWrongQuestion(UpdateWrongQuestionReq request);

    /**
     * 转换为题目采集任务实体。
     */
    QuestionCaptureTask toQuestionCaptureTask(QuestionCaptureTaskCreateReq request);

    /**
     * 转换为题目采集任务分页响应。
     */
    QuestionCaptureTaskPageListResp toQuestionCaptureTaskPageListResp(QuestionCaptureTask task);

    /**
     * 转换为题目采集任务详情响应。
     */
    QuestionCaptureTaskResp toQuestionCaptureTaskResp(QuestionCaptureTask task);

    /**
     * 转换为题目采集题块响应。
     */
    QuestionCaptureRegionResp toQuestionCaptureRegionResp(QuestionCaptureRegion region);

    /**
     * 转换为题目采集页面响应。
     */
    QuestionCapturePageResp toQuestionCapturePageResp(QuestionCapturePage page);

    /**
     * 复制题目采集题块，不保留主键和错题关联。
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wrongQuestionId", ignore = true)
    QuestionCaptureRegion copyQuestionCaptureRegion(QuestionCaptureRegion region);

    /**
     * 转换为错题订正记录响应。
     */
    WrongQuestionCorrectionRecordResp toWrongQuestionCorrectionRecordResp(WrongQuestionCorrectionRecord record);
}
