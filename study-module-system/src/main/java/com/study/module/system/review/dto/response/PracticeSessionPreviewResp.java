package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 专项练习组卷预览响应。
 */
@Data
public class PracticeSessionPreviewResp {

    @ApiModelProperty("请求题量")
    private Integer requestedQuestionCount;

    @ApiModelProperty("可组题量")
    private Integer availableQuestionCount;

    @ApiModelProperty("缺少题量")
    private Integer shortageQuestionCount;

    @ApiModelProperty("错题本题量")
    private Integer wrongQuestionCount;

    @ApiModelProperty("题库题量")
    private Integer bankQuestionCount;

    @ApiModelProperty("覆盖知识点")
    private List<String> learningPointList;

    @ApiModelProperty("组卷规则说明")
    private String generationReason;
}
