package com.study.module.system.msg.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 消息IDS请求类
 */
@Data
public class MsgIdsRequest {

    @ApiModelProperty("消息ID")
    @NotEmpty(message = "请选择至少一条消息")
    private List<Long> ids;
}