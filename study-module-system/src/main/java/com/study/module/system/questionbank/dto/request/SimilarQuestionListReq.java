package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 根据个人错题查询审核通过的相似题请求
 */
@Data
public class SimilarQuestionListReq {

    @ApiModelProperty("错题ID")
    @NotNull(message = "错题ID不能为空")
    private Long wrongQuestionId;

    @ApiModelProperty("返回数量")
    @Min(value = 3, message = "相似题数量不能少于{value}道")
    @Max(value = 5, message = "相似题数量不能超过{value}道")
    private Integer limit;

    @ApiModelProperty("推荐层级：SAME_PATTERN同型变式，CROSS_CONTEXT跨情境应用；为空兼容原同型推荐")
    @Pattern(regexp = "SAME_PATTERN|CROSS_CONTEXT", message = "推荐层级不正确")
    private String recommendationLevel;
}
