package com.study.module.system.dict.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 字典数据请求类
 */
@Data
public class DictDataCreateReq {

    @ApiModelProperty(value = "字典类型", required = true)
    @NotNull(message = "字典类型不为空")
    private String dictType;

    @ApiModelProperty(value = "字典键值", required = true)
    @NotNull(message = "字典键值不为空")
    private String dictValue;

    @ApiModelProperty(value = "字典标签", required = true)
    @NotBlank(message = "字典标签不为空")
    @Length(min = 1, max = 30, message = "字典标签在1到30字之间")
    private String dictLabel;

    @ApiModelProperty(value = "备注")
    @Length(max = 200, message = "字典数据备注在1到200字之间")
    private String remark;
}
