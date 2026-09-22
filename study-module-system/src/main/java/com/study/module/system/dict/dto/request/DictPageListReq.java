package com.study.module.system.dict.dto.request;

import com.study.common.core.domain.dto.PageParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 字典分页列表请求类
 */
@Data
public class DictPageListReq extends PageParam {

    @ApiModelProperty(value = "字典类型")
    private String dictType;

    @ApiModelProperty(value = "字典名称")
    private String dictName;
}
