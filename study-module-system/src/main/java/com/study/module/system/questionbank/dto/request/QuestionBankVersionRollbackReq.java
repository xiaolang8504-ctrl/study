package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 回退题目内容到一个历史保存版本。 */
@Data
public class QuestionBankVersionRollbackReq {
    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    @NotNull(message = "目标版本号不能为空")
    @ApiModelProperty("目标版本号")
    private Integer versionNo;

    @Size(max = 500, message = "回退说明不能超过500个字符")
    private String handleRemark;
}
