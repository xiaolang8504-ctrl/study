package com.study.module.system.dict.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 字典数据更新请求类
 */
@Data
public class DictDataUpdateReq extends DictDataCreateReq {

    @NotNull(message = "字典数据ID不为空")
    @Range(min = 1)
    @ApiModelProperty(value = "字典数据ID", required = true)
    private Integer id;
}
