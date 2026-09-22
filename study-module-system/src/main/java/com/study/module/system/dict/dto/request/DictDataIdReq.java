package com.study.module.system.dict.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 字典数据ID请求类
 */
@Data
public class DictDataIdReq {

    @ApiModelProperty(value = "字典数据ID")
    @NotNull(message = "字典数据ID不为空")
    @Range(min = 1)
    private Integer id;
}
