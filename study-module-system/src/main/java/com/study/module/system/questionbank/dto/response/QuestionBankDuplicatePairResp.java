package com.study.module.system.questionbank.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 历史重复题对
 */
@Data
public class QuestionBankDuplicatePairResp {

    @ApiModelProperty("第一道题")
    private QuestionBankDuplicateResp firstQuestion;

    @ApiModelProperty("第二道题")
    private QuestionBankDuplicateResp secondQuestion;

    @ApiModelProperty("相似度百分比")
    private Integer similarity;

    @ApiModelProperty("匹配类型")
    private String matchType;
}
