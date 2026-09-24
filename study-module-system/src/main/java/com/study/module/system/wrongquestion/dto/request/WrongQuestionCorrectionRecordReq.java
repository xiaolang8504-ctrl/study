package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 错题订正记录提交请求类
 */
@Data
public class WrongQuestionCorrectionRecordReq {

    @ApiModelProperty(value = "错题ID", required = true)
    @NotNull(message = "错题ID不能为空")
    @Range(min = 1, message = "错题ID需大于{min}")
    private Long wrongQuestionId;

    @ApiModelProperty("提交前独立思路")
    private String thinking;

    @ApiModelProperty("学生自述错因")
    private String errorReason;

    @ApiModelProperty("订正答案")
    private String correctionAnswer;

    @ApiModelProperty("订正解析")
    private String correctionAnalysis;

    @ApiModelProperty("订正图片地址")
    private String correctionImageUrl;

    @ApiModelProperty("订正备注")
    private String correctionRemark;
}
