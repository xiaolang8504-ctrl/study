package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 纸面练习卷逐题回填详情响应。
 */
@Data
public class PracticePaperFillDetailResp {

    private String paperCode;

    private Long sessionId;

    private String title;

    private Integer paperVersion;

    private Integer questionCount;

    @ApiModelProperty("最近一次纸面作答序号；0表示从未回填")
    private Integer latestAttemptNo;

    private Integer answeredCount;

    private Integer correctCount;

    private Integer wrongCount;

    private Integer unansweredCount;

    @ApiModelProperty("是否已有可覆盖的纸面回填记录")
    private Boolean hasPreviousRecord;

    private List<PracticePaperFillQuestionResp> questionList;
}
