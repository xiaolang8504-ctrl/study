package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 专项练习会话ID请求
 */
@Data
public class PracticeSessionIdReq {

    @ApiModelProperty(value = "练习会话ID", required = true)
    @NotNull(message = "练习会话ID不能为空")
    private Long sessionId;
}
