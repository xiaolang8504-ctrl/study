package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 复习提醒ID请求
 */
@Data
public class ReviewReminderIdReq {

    @ApiModelProperty(value = "复习提醒ID", required = true)
    @NotNull(message = "复习提醒ID不能为空")
    private Long id;
}
