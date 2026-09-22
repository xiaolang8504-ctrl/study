package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量提交专项练习作答请求
 */
@Data
@ApiModel("批量提交专项练习作答请求")
public class PracticeAnswerBatchSubmitReq {

    @ApiModelProperty(value = "练习会话ID", required = true)
    @NotNull(message = "练习会话ID不能为空")
    private Long sessionId;

    @ApiModelProperty(value = "作答列表", required = true)
    @Valid
    @NotEmpty(message = "作答列表不能为空")
    private List<PracticeAnswerSubmitReq> answerList;
}
