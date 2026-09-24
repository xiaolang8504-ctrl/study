package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/** 面向学生的可解释复习计划说明。 */
@Data
public class ReviewPlanExplanationResp {

    @ApiModelProperty("今日安排说明")
    private String todayReason;

    @ApiModelProperty("单题预计用时，单位分钟")
    private Integer estimatedMinutesPerQuestion;

    @ApiModelProperty("用时估算口径")
    private String timeEstimateReason;

    @ApiModelProperty("未来7日按当前排期的负荷")
    private List<ReviewFutureLoadResp> sevenDayLoadList;

    @ApiModelProperty("未来30日按当前排期的负荷")
    private List<ReviewFutureLoadResp> thirtyDayLoadList;

    @ApiModelProperty("负荷说明")
    private String loadNotice;
}
