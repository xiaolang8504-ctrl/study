package com.study.module.system.book.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 课本修改请求
 */
@Data
public class UpdateBookReq extends BookReq {

    @ApiModelProperty(value = "课本ID", required = true)
    @NotNull(message = "课本ID不能为空")
    @Range(min = 1, message = "课本ID需大于{min}")
    private Long id;
}
