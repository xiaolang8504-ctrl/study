package com.study.module.system.wrongquestion.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 能力层级统计响应。
 */
@Data
public class WrongQuestionAbilityLevelStatisticsResp {

    @ApiModelProperty("能力层级编码：FOUNDATION、APPLICATION、COMPREHENSIVE、UNMARKED")
    private String abilityLevel;

    @ApiModelProperty("能力层级名称")
    private String abilityLevelName;

    @ApiModelProperty("错题总数")
    private Integer wrongQuestionCount;

    @ApiModelProperty("待订正数量")
    private Integer pendingCorrectionCount;

    @ApiModelProperty("已掌握数量")
    private Integer masteredCount;
}
