package com.study.module.system.dict.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 字典数据排序请求类
 */
@Data
public class DictDataSortReq {

    @ApiModelProperty(value = "字典数据ID")
    @NotNull(message = "字典数据ID不为空")
    @Range(min = 1)
    private Integer id;

    @ApiModelProperty(value = "type: 0上移, 1下移")
    @NotNull(message = "type参数不为空")
    @Range(min = 0)
    private Integer type;
}
