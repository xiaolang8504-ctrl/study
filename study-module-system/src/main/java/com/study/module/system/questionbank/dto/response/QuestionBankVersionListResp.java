package com.study.module.system.questionbank.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 题目版本列表响应。
 */
@Data
public class QuestionBankVersionListResp {
    @ApiModelProperty("版本ID")
    private Long id;
    @ApiModelProperty("版本号")
    private Integer versionNo;
    @ApiModelProperty("操作类型：CREATE或UPDATE")
    private String operationType;
    private String questionTitle;
    private String questionContent;
    private String optionsJson;
    private String correctAnswer;
    private String analysis;
    private List<Long> knowledgePointIds;
    private Long operatorId;
    private LocalDateTime createTime;
}
