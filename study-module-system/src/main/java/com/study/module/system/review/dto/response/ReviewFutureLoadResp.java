package com.study.module.system.review.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/** 某日按当前排期计算的复习负荷。 */
@Data
public class ReviewFutureLoadResp {

    @ApiModelProperty("日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reviewDate;

    @ApiModelProperty("按当前排期到期的题数；已逾期题归入今天")
    private Integer dueCount;

    @ApiModelProperty("按个人用时估算的分钟数")
    private Integer estimatedMinutes;
}
