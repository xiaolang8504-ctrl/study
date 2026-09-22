package com.study.module.system.wrongquestion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题学习证据时间线响应。
 */
@Data
public class WrongQuestionTimelineResp {
    private Long id;
    @ApiModelProperty("事件类型")
    private String eventType;
    @ApiModelProperty("动作来源：STUDENT、OCR、AI、SYSTEM")
    private String eventSource;
    @ApiModelProperty("可展示的事件说明")
    private String eventContent;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
