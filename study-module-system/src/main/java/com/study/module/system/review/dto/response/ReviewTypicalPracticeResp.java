package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 典型错题练习响应
 */
@Data
public class ReviewTypicalPracticeResp {

    @ApiModelProperty("算法说明")
    private String recommendation;

    @ApiModelProperty("覆盖知识点数量")
    private Integer learningPointCount;

    @ApiModelProperty("实际生成题目数")
    private Integer questionCount;

    @ApiModelProperty("典型错题列表")
    private List<ReviewPracticeQuestionResp> questionList;
}
