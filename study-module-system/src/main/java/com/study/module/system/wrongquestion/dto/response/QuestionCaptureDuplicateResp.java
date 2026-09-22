package com.study.module.system.wrongquestion.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 采集题块命中的已有错题。
 */
@Data
public class QuestionCaptureDuplicateResp {

    private Long regionId;
    private Long wrongQuestionId;
    private String questionTitle;
    private String subject;
    private LocalDateTime createTime;
    private String matchType;
}
