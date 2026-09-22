package com.study.module.system.msg.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 消息ID请求类
 */
@Data
public class MsgIdReq {

    @ApiModelProperty("消息ID")
    @NotNull(message = "消息ID不能为空")
    @Range(min = 1, message = "消息ID需大于{min}")
    private Long id;
}
