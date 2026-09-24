package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 报告指标到可执行练习的下钻结果。
 */
@Data
public class ReviewReportActionDetailResp {

    @ApiModelProperty("行动类型：KNOWLEDGE_POINT、ERROR_LABEL、LOW_RETENTION")
    private String actionType;

    @ApiModelProperty("行动标题")
    private String title;

    @ApiModelProperty("行动说明")
    private String description;

    @ApiModelProperty("符合当前下钻条件的错题总数")
    private Integer questionCount;

    @ApiModelProperty("本次可加入组卷篮的题目，最多20题")
    private List<ReviewReportActionQuestionResp> questionList;
}
