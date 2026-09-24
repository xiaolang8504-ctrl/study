package com.study.module.system.review.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
@ApiModel("考前冲刺设置请求")
public class ReviewExamSprintSaveReq {
    @ApiModelProperty(value = "科目", required = true)
    @NotBlank(message = "科目不能为空")
    @Size(max = 50, message = "科目长度不能超过50个字符")
    private String subject;
    @ApiModelProperty(value = "考试日期", required = true)
    @NotNull(message = "考试日期不能为空")
    private LocalDate examDate;
    @ApiModelProperty("考试范围或章节")
    @Size(max = 500, message = "考试范围不能超过500个字符")
    private String scopeText;
    @ApiModelProperty(value = "每日可用分钟数", required = true)
    @NotNull(message = "每日可用时间不能为空")
    @Min(value = 5, message = "每日可用时间不能少于5分钟")
    @Max(value = 240, message = "每日可用时间不能超过240分钟")
    private Integer dailyMinutes;
    @ApiModelProperty(value = "每日短练题数", required = true)
    @NotNull(message = "每日短练题数不能为空")
    @Min(value = 1, message = "每日短练题数不能少于1")
    @Max(value = 30, message = "每日短练题数不能超过30")
    private Integer targetQuestionCount;
}
