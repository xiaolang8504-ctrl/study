package com.study.module.system.msg.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息详情响应类
 */
@Data
public class MsgDetailResp {

    @ApiModelProperty("消息标题")
    private String msgTitle;

    @ApiModelProperty("消息类型文本")
    private String msgTypeText;

    @ApiModelProperty("接收时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty("消息内容")
    private String msgContent;
}
