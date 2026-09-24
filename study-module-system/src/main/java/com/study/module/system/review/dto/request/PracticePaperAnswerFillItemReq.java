package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 纸面练习卷单题回填请求。
 */
@Data
@ApiModel("纸面练习卷单题回填请求")
public class PracticePaperAnswerFillItemReq {

    @ApiModelProperty(value = "练习题目ID", required = true)
    @NotNull(message = "练习题目ID不能为空")
    private Long sessionQuestionId;

    @ApiModelProperty(value = "CORRECT正确、WRONG错误、UNANSWERED未作答", required = true)
    @NotBlank(message = "请填写纸面作答结果")
    @Size(max = 16, message = "纸面作答结果不正确")
    private String answerStatus;

    @ApiModelProperty("纸面答案或解题步骤")
    @Size(max = 4000, message = "纸面答案不能超过4000个字符")
    private String studentAnswer;

    @ApiModelProperty("错因说明")
    @Size(max = 500, message = "错因说明不能超过500个字符")
    private String errorReason;

    @ApiModelProperty("作答用时，单位秒")
    @Min(value = 0, message = "作答用时不能小于0")
    @Max(value = 86400, message = "单题作答用时不能超过24小时")
    private Integer durationSeconds;
}
