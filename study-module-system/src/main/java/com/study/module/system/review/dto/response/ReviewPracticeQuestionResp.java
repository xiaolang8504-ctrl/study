package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 专项练习题目响应
 */
@Data
public class ReviewPracticeQuestionResp {

    @ApiModelProperty("错题ID")
    private Long wrongQuestionId;

    @ApiModelProperty("题目标题")
    private String questionTitle;

    @ApiModelProperty("题目内容")
    private String questionContent;

    @ApiModelProperty("科目名称")
    private String subjectName;

    @ApiModelProperty("题型名称")
    private String questionTypeName;

    @ApiModelProperty("知识点")
    private String learningPoint;

    @ApiModelProperty("难度")
    private Integer level;

    @ApiModelProperty("当前状态")
    private Integer status;

    @ApiModelProperty("题目图片")
    private String imageUrl;

    @ApiModelProperty("B选项图片")
    private String imageUrl2;

    @ApiModelProperty("C选项图片")
    private String imageUrl3;

    @ApiModelProperty("D选项图片")
    private String imageUrl4;

    @ApiModelProperty("入选原因")
    private String representativeReason;
}
