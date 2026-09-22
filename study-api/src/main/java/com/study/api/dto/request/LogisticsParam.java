package com.study.api.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 物流信息请求接口
 */
@Data
public class LogisticsParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("快递单号")
    private String expressNo;

    @ApiModelProperty("快递公司编号")
    private String expressCompanySn;

    @ApiModelProperty("手机号码")
    private String expressMobile;
}
