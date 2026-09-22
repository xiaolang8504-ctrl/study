package com.study.module.system.dict.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 字典更新请求类
 */
@Data
public class DictUpdateReq extends DictCreateReq {

    @NotNull(message = "字典ID不为空")
    @Range(min = 1)
    @ApiModelProperty(value = "字典ID", required = true)
    private Integer id;
}
