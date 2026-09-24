package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 以时间预算组织的今日学习任务包。
 */
@Data
public class ReviewTodayActionPackageResp {

    @ApiModelProperty("学生可投入的时间预算，单位分钟")
    private Integer budgetMinutes;

    @ApiModelProperty("任务包实际预计用时，单位分钟")
    private Integer estimatedMinutes;

    @ApiModelProperty("任务包标题")
    private String title;

    @ApiModelProperty("任务包推荐说明")
    private String recommendation;

    @ApiModelProperty("按建议顺序执行的学习步骤")
    private List<ReviewTodayActionResp> actionList;
}
