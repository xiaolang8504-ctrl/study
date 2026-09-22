package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 复习计划设置响应
 */
@Data
@ApiModel("复习计划设置响应")
public class ReviewPlanSettingResp {

    @ApiModelProperty("复习计划ID")
    private Long planId;

    @ApiModelProperty("计划名称")
    private String planName;

    @ApiModelProperty("每日复习题量上限")
    private Integer dailyLimit;

    @ApiModelProperty("是否开启站内提醒：0否，1是")
    private Integer reminderEnabled;

    @ApiModelProperty("每日提醒时间，格式HH:mm")
    private String reminderTime;

    @ApiModelProperty("复习星期：1周一至7周日")
    private List<Integer> reviewWeekDays;

    @ApiModelProperty("排期算法版本")
    private String algorithmVersion;

    @ApiModelProperty("科目复习设置")
    private List<ReviewSubjectSettingResp> subjectSettings;
}
