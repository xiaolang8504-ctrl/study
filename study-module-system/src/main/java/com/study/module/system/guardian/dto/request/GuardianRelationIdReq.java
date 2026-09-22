package com.study.module.system.guardian.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/** 监护关系标识请求。 */
@Data
public class GuardianRelationIdReq {

    @ApiModelProperty(value = "监护关系ID", required = true)
    @NotNull(message = "监护关系ID不能为空")
    private Long relationId;
}
