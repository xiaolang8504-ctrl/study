package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 学情报告查询请求。
 */
@Data
public class ReviewLearningReportReq {

    @ApiModelProperty("科目字典键值，为空时汇总全部科目")
    private String subject;
}
