package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * 学情报告查询请求。
 */
@Data
public class ReviewLearningReportReq {

    @ApiModelProperty("科目字典键值，为空时汇总全部科目")
    private String subject;

    @ApiModelProperty("报告行动下钻类型：KNOWLEDGE_POINT、ERROR_LABEL、LOW_RETENTION")
    @Pattern(regexp = "KNOWLEDGE_POINT|ERROR_LABEL|LOW_RETENTION", message = "行动下钻类型不正确")
    private String actionType;

    @ApiModelProperty("知识点下钻目标ID，actionType=KNOWLEDGE_POINT 时必填")
    private Long knowledgePointId;

    @ApiModelProperty("错因下钻目标，actionType=ERROR_LABEL 时必填")
    private String errorLabel;
}
