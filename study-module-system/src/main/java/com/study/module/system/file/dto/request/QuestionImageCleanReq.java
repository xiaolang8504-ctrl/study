package com.study.module.system.file.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;

/**
 * 题目孤立图片清理请求
 */
@Data
public class QuestionImageCleanReq {

    @ApiModelProperty("保留小时数，默认24小时")
    @Min(value = 1, message = "保留时间不能小于1小时")
    private Integer retentionHours = 24;
}
