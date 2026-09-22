package com.study.common.core.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
import javax.validation.constraints.NotNull;

/**
 * 翻页请求数据
 */
@Data
public class PageParam {

    @ApiModelProperty(value = "当前页码", required = true)
    @NotNull(message = "当前页码不能为空")
    @Range(min = 1)
    private Integer current;

    @ApiModelProperty(value = "每页显示数量", required = true)
    @NotNull(message = "每页显示数量不能为空")
    @Range(min = 1, max = 100)
    private Integer pageSize;

    @ApiModelProperty("关键词")
    private String keyWord;
}
