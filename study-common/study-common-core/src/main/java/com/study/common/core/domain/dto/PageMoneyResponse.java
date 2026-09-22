package com.study.common.core.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 带金额统计翻页响应数据
 */
@Data
public class PageMoneyResponse<T> extends PageResult<T> {

    @ApiModelProperty("含税总金额")
    private BigDecimal taxTotalMoney;

    @ApiModelProperty("不含税总金额")
    private BigDecimal noTaxTotalMoney;

    @ApiModelProperty("累计下单金额")
    private BigDecimal orderTotalMoney;

    @ApiModelProperty("累计采购金额")
    private BigDecimal buyTotalMoney;

    @ApiModelProperty("累计交付金额")
    private BigDecimal deliverTotalMoney;

    @ApiModelProperty("累计开票金额")
    private BigDecimal invoiceMoney;
}
