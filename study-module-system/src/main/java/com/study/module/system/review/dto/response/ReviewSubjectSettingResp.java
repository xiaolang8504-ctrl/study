package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 复习计划科目设置响应
 */
@Data
@ApiModel("复习计划科目设置响应")
public class ReviewSubjectSettingResp {

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("科目名称")
    private String subjectName;

    @ApiModelProperty("是否参与智能复习：0否，1是")
    private Integer enabled;

    @ApiModelProperty("科目每日复习题量上限")
    private Integer dailyLimit;
}
