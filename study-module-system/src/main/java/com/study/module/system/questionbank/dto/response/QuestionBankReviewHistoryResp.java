package com.study.module.system.questionbank.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目审核历史响应。
 */
@Data
public class QuestionBankReviewHistoryResp {
    private Long id;
    @ApiModelProperty("审核状态：1通过，2驳回")
    private Integer reviewStatus;
    private String reviewRemark;
    private Long reviewerId;
    @ApiModelProperty("审核人名称")
    private String reviewerName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;
}
