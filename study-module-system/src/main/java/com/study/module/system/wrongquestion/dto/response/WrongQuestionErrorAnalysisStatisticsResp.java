package com.study.module.system.wrongquestion.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 错因分析统计响应类
 */
@Data
public class WrongQuestionErrorAnalysisStatisticsResp {

    @ApiModelProperty("错误类型标签")
    private String errorLabel;

    @ApiModelProperty("错题总数")
    private Integer wrongQuestionCount;

    @ApiModelProperty("待订正数量")
    private Integer pendingCorrectionCount;

    @ApiModelProperty("已订正数量")
    private Integer correctedCount;

    @ApiModelProperty("已掌握数量")
    private Integer masteredCount;

    @ApiModelProperty("已归档数量")
    private Integer archivedCount;
}
