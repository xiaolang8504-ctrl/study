package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 更新复习计划设置请求
 */
@Data
@ApiModel("更新复习计划设置请求")
public class UpdateReviewPlanSettingReq {

    @ApiModelProperty(value = "计划名称", required = true)
    @NotBlank(message = "计划名称不能为空")
    @Size(max = 50, message = "计划名称不能超过50个字符")
    private String planName;

    @ApiModelProperty(value = "每日复习题量上限", required = true)
    @NotNull(message = "每日复习题量上限不能为空")
    @Min(value = 1, message = "每日复习题量上限不能小于1")
    @Max(value = 200, message = "每日复习题量上限不能超过200")
    private Integer dailyLimit;

    @ApiModelProperty(value = "是否开启站内提醒：0否，1是", required = true)
    @NotNull(message = "请选择是否开启站内提醒")
    @Min(value = 0, message = "提醒开关参数不正确")
    @Max(value = 1, message = "提醒开关参数不正确")
    private Integer reminderEnabled;

    @ApiModelProperty(value = "每日提醒时间，格式HH:mm", required = true)
    @NotBlank(message = "提醒时间不能为空")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "提醒时间格式不正确")
    private String reminderTime;

    @ApiModelProperty(value = "复习星期：1周一至7周日", required = true)
    @NotEmpty(message = "请至少选择一个复习日")
    @Size(max = 7, message = "复习日设置不正确")
    private List<@Min(value = 1, message = "复习日设置不正确")
            @Max(value = 7, message = "复习日设置不正确") Integer> reviewWeekDays;

    @ApiModelProperty(value = "科目复习设置", required = true)
    @NotEmpty(message = "请至少设置一个复习科目")
    @Valid
    private List<ReviewSubjectSettingReq> subjectSettings;
}
