package com.study.module.system.guardian.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/** 保存某一学生的一周沟通计划。 */
@Data
public class GuardianWeeklyPlanSaveReq {
    @ApiModelProperty("周计划ID，为空时新增")
    private Long id;

    @NotNull(message = "学生用户ID不能为空")
    private Long studentUserId;

    @ApiModelProperty("周一日期，默认当前周周一")
    private LocalDate weekStartDate;

    @NotBlank(message = "周计划标题不能为空")
    @Size(max = 120, message = "周计划标题不能超过120个字符")
    private String planTitle;

    @NotBlank(message = "周计划内容不能为空")
    @Size(max = 2000, message = "周计划内容不能超过2000个字符")
    private String planContent;

    @Min(value = 0, message = "目标复习次数不能小于0")
    @Max(value = 99, message = "目标复习次数不能超过99")
    private Integer targetReviewCount;
}
