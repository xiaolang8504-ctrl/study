package com.study.module.system.dict.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典数据分页响应类
 */
@Data
public class DictDataListResp {

    @ApiModelProperty(value = "字典数据ID")
    private Integer id;

    @ApiModelProperty(value = "字典标签")
    private String dictLabel;

    @ApiModelProperty(value = "字典键值")
    private String dictValue;

    @ApiModelProperty(value = "是否启用: 0否, 1是")
    private Integer isEnable;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
