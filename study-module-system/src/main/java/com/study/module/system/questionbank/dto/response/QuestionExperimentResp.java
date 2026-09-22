package com.study.module.system.questionbank.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * A/B实验配置响应。
 */
@Data
public class QuestionExperimentResp {
    private Long id;
    private String experimentName;
    private Integer enable;
    private Integer groupATraffic;
    private Integer groupBTraffic;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private String remark;
    private Boolean running;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
