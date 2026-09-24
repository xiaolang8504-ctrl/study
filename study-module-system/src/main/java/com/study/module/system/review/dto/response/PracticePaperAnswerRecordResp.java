package com.study.module.system.review.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 纸面练习卷单题回填记录响应。
 */
@Data
public class PracticePaperAnswerRecordResp {

    private Long id;

    @ApiModelProperty("第几次纸面作答")
    private Integer attemptNo;

    @ApiModelProperty("CORRECT正确、WRONG错误、UNANSWERED未作答")
    private String answerStatus;

    private String studentAnswer;

    private String errorReason;

    private Integer durationSeconds;

    private Long answerFileId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime answerTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
