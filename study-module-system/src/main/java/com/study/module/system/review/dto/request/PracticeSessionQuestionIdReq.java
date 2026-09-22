package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 专项练习题目ID请求。
 */
@Data
public class PracticeSessionQuestionIdReq {

    @ApiModelProperty(value = "练习题目ID", required = true)
    @NotNull(message = "练习题目ID不能为空")
    private Long sessionQuestionId;
}
