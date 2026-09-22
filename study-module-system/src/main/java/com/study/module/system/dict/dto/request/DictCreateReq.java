package com.study.module.system.dict.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 字典创建请求类
 */
@Data
public class DictCreateReq {

    @NotBlank(message = "字典类型不为空")
    @Length(min = 1, max = 30, message = "字典类型在1到30字之间")
    @ApiModelProperty("字典类型")
    private String dictType;

    @NotBlank(message = "字典名称不为空")
    @Length(min = 1, max = 50, message = "字典名称在1到50字之间")
    @ApiModelProperty("字典名称")
    private String dictName;

    @Length(max = 200, message = "字典备注在1到200字之间")
    @ApiModelProperty("备注")
    private String remark;
}
