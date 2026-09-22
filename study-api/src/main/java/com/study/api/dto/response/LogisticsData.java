package com.study.api.dto.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 快递详情查询条件
 */
@Data
public class LogisticsData implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 快递状态
     */
    Integer expressStatus;

    /**
     * 物流详情
     */
    String expressInfo;
}
