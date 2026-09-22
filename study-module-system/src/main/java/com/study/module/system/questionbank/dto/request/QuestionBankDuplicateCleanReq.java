package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 历史重复题清理请求
 */
@Data
public class QuestionBankDuplicateCleanReq {

    @ApiModelProperty("保留的题目ID")
    @NotNull(message = "保留题目ID不能为空")
    private Long keepId;

    @ApiModelProperty("删除的重复题ID")
    @NotNull(message = "删除题目ID不能为空")
    private Long deleteId;
}
