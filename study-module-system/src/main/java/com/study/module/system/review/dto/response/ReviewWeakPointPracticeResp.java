package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 薄弱知识点专项练习响应
 */
@Data
public class ReviewWeakPointPracticeResp {

    @ApiModelProperty("知识点")
    private String learningPoint;

    @ApiModelProperty("建议文案")
    private String recommendation;

    @ApiModelProperty("实际生成题目数")
    private Integer questionCount;

    @ApiModelProperty("专项练习题目")
    private List<ReviewPracticeQuestionResp> questionList;
}
