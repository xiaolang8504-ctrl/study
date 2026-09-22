package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 专项练习参考答案响应。
 */
@Data
public class PracticeQuestionAnswerResp {

    @ApiModelProperty("标准答案")
    private String correctAnswer;

    @ApiModelProperty("题目解析")
    private String analysis;

    @ApiModelProperty("是否由系统自动判题")
    private Boolean autoJudge;
}
