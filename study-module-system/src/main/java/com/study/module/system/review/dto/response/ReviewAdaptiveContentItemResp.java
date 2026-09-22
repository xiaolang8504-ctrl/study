package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 自适应学习内容项
 */
@Data
public class ReviewAdaptiveContentItemResp {

    @ApiModelProperty("错题ID")
    private Long wrongQuestionId;

    @ApiModelProperty("题目标题")
    private String questionTitle;

    @ApiModelProperty("科目")
    private String subject;

    @ApiModelProperty("科目名称")
    private String subjectName;

    @ApiModelProperty("知识点")
    private String learningPoint;

    @ApiModelProperty("难度")
    private Integer level;

    @ApiModelProperty("推荐动作: REVIEW复习, CONSOLIDATE巩固, CHALLENGE提升")
    private String actionType;

    @ApiModelProperty("推荐动作文本")
    private String actionText;

    @ApiModelProperty("推荐原因")
    private String recommendationReason;
}
