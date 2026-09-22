package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 专项练习作答响应
 */
@Data
public class PracticeAnswerSubmitResp {

    @ApiModelProperty("是否正确")
    private Boolean correct;

    @ApiModelProperty("正确答案")
    private String correctAnswer;

    @ApiModelProperty("解析")
    private String analysis;

    @ApiModelProperty("判题方式")
    private String judgeType;

    @ApiModelProperty("是否自动沉淀错题")
    private Boolean autoCollectedWrongQuestion;

    @ApiModelProperty("沉淀后的错题ID")
    private Long wrongQuestionId;
}
