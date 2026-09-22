package com.study.module.system.questionbank.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * A/B实验历史响应。
 */
@Data
public class QuestionExperimentHistoryResp extends QuestionExperimentResp {
    private Long historyId;
    private Long operatorId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime operationTime;
    private Long groupAAnsweredCount;
    private Double groupACorrectRate;
    private Long groupBAnsweredCount;
    private Double groupBCorrectRate;
    private Double lift;
    private Double pValue;
    private Boolean significant;
}
