package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 错题错因分析请求类
 */
@Data
public class WrongQuestionErrorAnalysisReq {

    @ApiModelProperty(value = "错题ID", required = true)
    @NotNull(message = "错题ID不能为空")
    @Range(min = 1, message = "错题ID需大于{min}")
    private Long id;

    @ApiModelProperty("错误类型标签，多个标签用逗号分隔")
    private String errorLabels;

    @ApiModelProperty("结构化错因编码，多个用逗号分隔：READING、CONCEPT、METHOD、CALCULATION、EXPRESSION")
    @Pattern(regexp = "^$|^(READING|CONCEPT|METHOD|CALCULATION|EXPRESSION)(,(READING|CONCEPT|METHOD|CALCULATION|EXPRESSION))*$", message = "结构化错因编码不正确")
    private String errorCauseCodes;

    @ApiModelProperty("能力层级：FOUNDATION、APPLICATION、COMPREHENSIVE")
    @Pattern(regexp = "FOUNDATION|APPLICATION|COMPREHENSIVE", message = "能力层级不正确")
    private String abilityLevel;

    @ApiModelProperty("错误原因")
    private String wrongReason;
}
