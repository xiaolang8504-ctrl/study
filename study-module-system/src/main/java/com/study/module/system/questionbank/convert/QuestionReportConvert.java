package com.study.module.system.questionbank.convert;

import com.study.module.system.questionbank.dto.response.QuestionReportPageListResp;
import com.study.module.system.questionbank.entity.QuestionReport;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 题目举报数据转换类
 */
@Mapper
public interface QuestionReportConvert {

    QuestionReportConvert INSTANCE = Mappers.getMapper(QuestionReportConvert.class);

    /**
     * 转换为题目举报响应
     */
    QuestionReportPageListResp toQuestionReportPageListResp(QuestionReport questionReport);
}
