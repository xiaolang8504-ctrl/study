package com.study.api.provider;


import com.study.common.core.exception.LogicException;

/**
 * 技术文档服务
 */
public interface DocumentProvider {

    /**
     * 清除删除时间大于等于90天的技术文档
     */
    void clearDeleteGeNinetyDayDocument() throws LogicException;
}
