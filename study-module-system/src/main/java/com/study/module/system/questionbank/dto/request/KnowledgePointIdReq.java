package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 知识点ID请求。
 */
@Data
public class KnowledgePointIdReq {
    @ApiModelProperty(value = "知识点ID", required = true)
    @NotNull(message = "知识点ID不能为空")
    private Long id;
}
