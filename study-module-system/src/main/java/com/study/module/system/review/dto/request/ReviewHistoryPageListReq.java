package com.study.module.system.review.dto.request;

import com.study.common.core.domain.dto.PageParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDate;

/**
 * 复习历史分页请求
 */
@Data
public class ReviewHistoryPageListReq extends PageParam {

    @ApiModelProperty("科目字典键值")
    private String subject;

    @ApiModelProperty("四级反馈：0忘记，1困难，2掌握，3很简单")
    @Min(value = 0, message = "反馈类型不正确")
    @Max(value = 3, message = "反馈类型不正确")
    private Integer feedback;

    @ApiModelProperty("开始日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @ApiModelProperty("结束日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
}
