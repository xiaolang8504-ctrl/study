package com.study.module.system.review.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 每日复习排期响应类
 */
@Data
public class ReviewScheduleResp {

    @ApiModelProperty("日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate reviewDate;

    @ApiModelProperty("待复习数量")
    private Long dueCount;

    @ApiModelProperty("已完成数量")
    private Long completedCount;
}
