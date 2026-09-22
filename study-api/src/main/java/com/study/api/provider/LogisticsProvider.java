package com.study.api.provider;

import com.study.api.dto.request.LogisticsParam;
import com.study.api.dto.response.LogisticsData;
import com.study.common.core.exception.LogicException;

/**
 * 接口服务
 */
public interface LogisticsProvider {

    /**
     * 通过查询条件获取物流信息
     */
    LogisticsData getLogisticsDetail(LogisticsParam logisticsParam) throws LogicException;
}
