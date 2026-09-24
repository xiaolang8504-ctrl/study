package com.study.module.system.wrongquestion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 同题一次错误来源响应。 */
@Data
public class WrongQuestionOccurrenceResp {
    private Long id;
    private Long originWrongQuestionId;
    private String wrongAnswer;
    private String source;
    private String sourceName;
    private Long captureTaskId;
    private Long capturePageId;
    private Long captureRegionId;
    private Integer captureSourcePageNo;
    private Integer leftPosition;
    private Integer topPosition;
    private Integer width;
    private Integer height;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime occurredAt;
}
