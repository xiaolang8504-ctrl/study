package com.study.module.system.wrongquestion.dto.response;

import lombok.Data;

/** 分层答案内容。 */
@Data
public class WrongQuestionAnswerLayerResp {
    private String layer;
    private String title;
    private String content;
}
