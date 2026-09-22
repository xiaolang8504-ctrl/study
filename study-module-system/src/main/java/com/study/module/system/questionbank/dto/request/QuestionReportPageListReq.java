package com.study.module.system.questionbank.dto.request;

import io.swagger.annotations.ApiModelProperty;
import com.study.common.core.domain.dto.PageParam;
import lombok.Data;

/**
 * 题目举报分页列表请求
 */
@Data
public class QuestionReportPageListReq extends PageParam {

    @ApiModelProperty("处理状态")
    private Integer status;

    @ApiModelProperty("举报类型")
    private String reportType;

    @ApiModelProperty("搜索关键字")
    private String keyWord;
}
