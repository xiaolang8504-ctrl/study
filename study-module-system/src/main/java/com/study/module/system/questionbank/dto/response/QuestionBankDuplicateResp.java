package com.study.module.system.questionbank.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 重复题检测结果
 */
@Data
public class QuestionBankDuplicateResp {

    @ApiModelProperty("题目ID")
    private Long id;

    @ApiModelProperty("题目标题")
    private String questionTitle;

    @ApiModelProperty("题目内容")
    private String questionContent;

    @ApiModelProperty("相似度百分比")
    private Integer similarity;

    @ApiModelProperty("匹配类型：EXACT精确，HIGH_SIMILAR高相似")
    private String matchType;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
