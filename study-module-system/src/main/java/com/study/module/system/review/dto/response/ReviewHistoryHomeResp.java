package com.study.module.system.review.dto.response;

import com.study.common.core.domain.dto.PageResult;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 复习历史首页响应
 */
@Data
public class ReviewHistoryHomeResp {

    @ApiModelProperty("累计复习次数")
    private Long totalReviewCount;

    @ApiModelProperty("本周复习次数")
    private Long weekReviewCount;

    @ApiModelProperty("正向反馈率")
    private Integer positiveFeedbackRate;

    @ApiModelProperty("真实作答正确率")
    private Integer correctRate;

    @ApiModelProperty("平均主动回忆用时，单位秒")
    private Integer averageDurationSeconds;

    @ApiModelProperty("复习历史分页")
    private PageResult<ReviewHistoryPageListResp> pageResult;
}
