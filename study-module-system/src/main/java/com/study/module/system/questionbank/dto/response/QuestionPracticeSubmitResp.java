package com.study.module.system.questionbank.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 题目练习提交响应
 */
@Data
public class QuestionPracticeSubmitResp {

    @ApiModelProperty("是否回答正确")
    private Boolean correct;

    @ApiModelProperty("正确答案")
    private String correctAnswer;

    @ApiModelProperty("答案解析")
    private String analysis;

    @ApiModelProperty("判题方式")
    private String judgeType;

    @ApiModelProperty("答错后自动加入错题本的错题ID")
    private Long wrongQuestionId;

    @ApiModelProperty("是否已自动加入错题本")
    private Boolean autoCollectedWrongQuestion;
}
