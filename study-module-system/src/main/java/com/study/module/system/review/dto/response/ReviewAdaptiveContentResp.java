package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 自适应学习内容响应
 */
@Data
public class ReviewAdaptiveContentResp {

    @ApiModelProperty("策略说明")
    private String strategyDescription;

    @ApiModelProperty("推荐内容")
    private List<ReviewAdaptiveContentItemResp> contentList;
}
