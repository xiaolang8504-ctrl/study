package com.study.common.core.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

/**
 * 翻页响应数据
 */
@Data
public class PageResult<T> {

    @ApiModelProperty("总页数")
    private long page;

    @ApiModelProperty("总条数")
    private long total;

    @ApiModelProperty("每页显示数量")
    private long pageSize;

    @ApiModelProperty("当前页码")
    private long current;

    @ApiModelProperty("数据集")
    private List<T> list;
}
