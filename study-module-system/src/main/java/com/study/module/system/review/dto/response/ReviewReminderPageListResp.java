package com.study.module.system.review.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 复习提醒分页响应
 */
@Data
public class ReviewReminderPageListResp {

    @ApiModelProperty("提醒ID")
    private Long id;

    @ApiModelProperty("站内消息ID")
    private Long msgId;

    @ApiModelProperty("消息标题")
    private String title;

    @ApiModelProperty("消息内容")
    private String content;

    @ApiModelProperty("到期数量")
    private Integer dueCount;

    @ApiModelProperty("逾期数量")
    private Integer overdueCount;

    @ApiModelProperty("各科任务数量摘要")
    private String subjectSummary;

    @ApiModelProperty("是否已读：0否，1是")
    private Integer isRead;

    @ApiModelProperty("发送时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentTime;
}
