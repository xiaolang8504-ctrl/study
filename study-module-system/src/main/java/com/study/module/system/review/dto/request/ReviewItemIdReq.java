package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 复习项目ID请求类
 */
@Data
public class ReviewItemIdReq {

    @ApiModelProperty(value = "复习项目ID", required = true)
    @NotNull(message = "复习项目ID不能为空")
    private Long reviewItemId;
}
