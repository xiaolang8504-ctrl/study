package com.study.module.system.homework.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 作业修改请求
 */
@Data
public class UpdateHomeWorkReq extends HomeWorkReq {

    @ApiModelProperty(value = "作业ID", required = true)
    @NotNull(message = "作业ID不能为空")
    @Range(min = 1, message = "作业ID需大于{min}")
    private Long id;
}
