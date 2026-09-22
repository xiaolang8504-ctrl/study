package com.study.module.system.dict.dto.request;

import com.study.common.core.domain.dto.PageParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 字典数据分页请求数据
 */
@Data
public class DictDataPageListReq extends PageParam {

    @ApiModelProperty("字典类型")
    private String dictType;

    @ApiModelProperty("字典数据名称")
    private String dictLabel;

    @ApiModelProperty("是否启用: 0否, 1是")
    private Integer isEnable;
}
