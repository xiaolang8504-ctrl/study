package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 纸面练习卷回填题目响应。
 */
@Data
public class PracticePaperFillQuestionResp {

    private Long sessionQuestionId;

    private Integer sortNo;

    private Long wrongQuestionId;

    private String questionSource;

    private String questionTitle;

    private String questionContent;

    private String contentFormat;

    private String optionsJson;

    private String subjectName;

    private String learningPoint;

    @ApiModelProperty("该题的纸面回填历史，按最近作答优先")
    private List<PracticePaperAnswerRecordResp> answerRecordList;
}
