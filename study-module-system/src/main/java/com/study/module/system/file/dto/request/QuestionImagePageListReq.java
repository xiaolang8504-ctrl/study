package com.study.module.system.file.dto.request;

import com.study.common.core.domain.dto.PageParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 题目图片分页请求
 */
@Data
public class QuestionImagePageListReq extends PageParam {

    @ApiModelProperty("引用状态：0未引用，1已引用")
    private Integer referenced;
}
