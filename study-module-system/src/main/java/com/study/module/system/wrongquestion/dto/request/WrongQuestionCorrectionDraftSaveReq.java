package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/** 自动保存订正草稿请求。 */
@Data
public class WrongQuestionCorrectionDraftSaveReq {

    @NotNull(message = "错题ID不能为空")
    @Range(min = 1, message = "错题ID需大于{min}")
    private Long wrongQuestionId;
    @ApiModelProperty("独立思路")
    @Size(max = 5000, message = "独立思路不能超过5000个字符")
    private String thinking;
    @ApiModelProperty("学生自述错因")
    @Size(max = 5000, message = "错因不能超过5000个字符")
    private String errorReason;
    private String correctionAnswer;
    private String correctionAnalysis;
    private String correctionImageUrl;
    @Size(max = 500, message = "备注不能超过500个字符")
    private String correctionRemark;
}
