package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * 题库题目编号请求
 */
@Data
public class QuestionBankIdReq {

    @ApiModelProperty("题库题目ID")
    @NotNull(message = "题目ID不能为空")
    private Long id;
}
