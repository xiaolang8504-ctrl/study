package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 复习计划科目设置请求
 */
@Data
@ApiModel("复习计划科目设置请求")
public class ReviewSubjectSettingReq {

    @ApiModelProperty(value = "科目字典键值", required = true)
    @NotBlank(message = "科目不能为空")
    private String subject;

    @ApiModelProperty(value = "是否参与智能复习：0否，1是", required = true)
    @NotNull(message = "请选择科目是否参与智能复习")
    @Min(value = 0, message = "科目启用状态不正确")
    @Max(value = 1, message = "科目启用状态不正确")
    private Integer enabled;

    @ApiModelProperty(value = "科目每日复习题量上限", required = true)
    @NotNull(message = "科目每日复习题量上限不能为空")
    @Min(value = 1, message = "科目每日复习题量上限不能小于1")
    @Max(value = 200, message = "科目每日复习题量上限不能超过200")
    private Integer dailyLimit;
}
