package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 错题修改请求类
 */
@Data
public class UpdateWrongQuestionReq extends WrongQuestionReq {

    @ApiModelProperty(value = "错题ID", required = true)
    @NotNull(message = "错题ID不能为空")
    @Range(min = 1, message = "错题ID需大于{min}")
    private Long id;
}
