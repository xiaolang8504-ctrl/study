package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 历史重复题扫描请求
 */
@Data
public class QuestionBankDuplicateHistoryReq {

    @ApiModelProperty("年级字典键值")
    private String grade;

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("相似度阈值，默认82")
    @Min(50)
    @Max(100)
    private Integer similarityThreshold;

    @ApiModelProperty("最多返回数量，默认100")
    @Min(1)
    @Max(500)
    private Integer limit;
}
