package com.study.module.system.wrongquestion.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/** 订正草稿响应。 */
@Data
public class WrongQuestionCorrectionDraftResp {
    private Long wrongQuestionId;
    private String thinking;
    private String errorReason;
    private String correctionAnswer;
    private String correctionAnalysis;
    private String correctionImageUrl;
    private String correctionRemark;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
