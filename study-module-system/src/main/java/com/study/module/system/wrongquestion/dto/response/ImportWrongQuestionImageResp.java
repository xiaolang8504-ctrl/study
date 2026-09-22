package com.study.module.system.wrongquestion.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * A4图片导入错题响应类
 */
@Data
public class ImportWrongQuestionImageResp {

    @ApiModelProperty("导入数量")
    private Integer importCount;

    @ApiModelProperty("异步采集任务ID；任务完成后在采集中心确认题块")
    private Long taskId;
}
