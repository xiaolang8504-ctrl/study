package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 错题状态更新请求类
 */
@Data
public class WrongQuestionStatusUpdateReq {

    @ApiModelProperty(value = "错题ID", required = true)
    @NotNull(message = "错题ID不能为空")
    @Range(min = 1, message = "错题ID需大于{min}")
    private Long id;

    @ApiModelProperty(value = "目标状态: 0待订正, 1已订正, 2已掌握, 3已归档", required = true)
    @NotNull(message = "目标状态不能为空")
    @Range(min = 0, max = 3, message = "状态值必须在{min}-{max}之间")
    private Integer status;

    @ApiModelProperty("状态变更备注")
    private String remark;
}
