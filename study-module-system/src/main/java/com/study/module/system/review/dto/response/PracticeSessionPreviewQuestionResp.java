package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 练习卷预览题目。
 */
@Data
public class PracticeSessionPreviewQuestionResp {

    @ApiModelProperty("题目来源")
    private String questionSource;

    @ApiModelProperty("题目ID")
    private Long questionId;

    @ApiModelProperty("题目标题")
    private String questionTitle;

    @ApiModelProperty("科目")
    private String subjectName;

    @ApiModelProperty("知识点")
    private String learningPoint;

    @ApiModelProperty("难度")
    private Integer difficulty;

    @ApiModelProperty("入选依据")
    private String sourceReason;
}
