package com.study.module.system.questionbank.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * A/B实验保存请求。
 */
@Data
public class QuestionExperimentSaveReq {
    private Long id;
    @NotBlank(message = "实验名称不能为空")
    private String experimentName;
    @NotNull(message = "启停状态不能为空")
    @Min(0) @Max(1)
    private Integer enable;
    @NotNull(message = "A组流量比例不能为空")
    @Min(0) @Max(100)
    private Integer groupATraffic;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    @ApiModelProperty("实验备注")
    private String remark;
}
