package com.study.module.system.msg.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息分页列表响应类
 */
@Data
public class MsgPageListResp {

    @ApiModelProperty("消息ID")
    private Long id;

    @ApiModelProperty("消息标题")
    private String msgTitle;

    @ApiModelProperty("消息类型文本")
    private String msgTypeText;

    @ApiModelProperty("是否已读: 0否, 1是")
    private Integer isRead;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
