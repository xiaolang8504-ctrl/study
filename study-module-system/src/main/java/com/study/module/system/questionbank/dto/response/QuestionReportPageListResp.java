package com.study.module.system.questionbank.dto.response;

import io.swagger.annotations.ApiModelProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 题目举报分页列表响应
 */
@Data
public class QuestionReportPageListResp {

    @ApiModelProperty("题目举报ID")
    private Long id;

    @ApiModelProperty("举报用户ID")
    private Long userId;

    @ApiModelProperty("题库题目ID")
    private Long bankQuestionId;

    @ApiModelProperty("题目标题")
    private String questionTitle;

    @ApiModelProperty("举报类型")
    private String reportType;

    @ApiModelProperty("举报内容")
    private String reportContent;

    @ApiModelProperty("处理状态")
    private Integer status;

    @ApiModelProperty("处理人ID")
    private Long handlerId;

    @ApiModelProperty("处理备注")
    private String handleRemark;

    @ApiModelProperty("处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime handleTime;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
