package com.study.module.system.review.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 查看复习答案响应类
 */
@Data
public class ReviewAnswerResp {

    @ApiModelProperty("复习项目ID")
    private Long reviewItemId;

    @ApiModelProperty("错误答案")
    private String wrongAnswer;

    @ApiModelProperty("正确答案")
    private String correctAnswer;

    @ApiModelProperty("题目解析")
    private String analysis;

    @ApiModelProperty("查看答案时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime revealTime;

    @ApiModelProperty("服务端答案解锁凭证")
    private String revealToken;
}
