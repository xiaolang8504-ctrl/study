package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 相似题作答提交请求
 */
@Data
public class QuestionPracticeSubmitReq {

    @ApiModelProperty("推荐记录ID")
    @NotNull(message = "推荐记录ID不能为空")
    private Long recommendationId;

    @ApiModelProperty("学生答案")
    @NotBlank(message = "学生答案不能为空")
    private String studentAnswer;

    @ApiModelProperty("作答用时，单位秒")
    private Integer durationSeconds;

    /**
     * 主观题由学生对照标准答案后自评；客观题无需传值。
     */
    @ApiModelProperty("主观题学生自评结果")
    private Boolean selfCorrect;
}
