package com.yunshang.budget.common.mybatis.utils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.common.core.domain.dto.PageMoneyResponse;
import com.study.common.core.domain.dto.PageResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

/**
 * 分页工具
 */
public class PageUtils {

    /**
     * 组装分页响应数据
     */
    public static <T,R> PageResult<R> wrap(Page<T> page, Function<List<T>, List<R>> callback) {
        PageResult<R> response = new PageResult<>();
        fillPageData(response, page, callback);
        return response;
    }

    /**
     * 组装分页响应数据
     */
    public static <T,R> PageMoneyResponse<R> wrap(Page<T> page, Function<List<T>, List<R>> callback, BigDecimal taxTotalMoney, BigDecimal noTaxTotalMoney, BigDecimal orderTotalMoney, BigDecimal buyTotalMoney, BigDecimal deliverTotalMoney, BigDecimal invoiceMoney) {
        PageMoneyResponse<R> response = new PageMoneyResponse<>();
        fillPageData(response, page, callback);
        response.setTaxTotalMoney(taxTotalMoney);
        response.setNoTaxTotalMoney(noTaxTotalMoney);
        response.setOrderTotalMoney(orderTotalMoney);
        response.setBuyTotalMoney(buyTotalMoney);
        response.setDeliverTotalMoney(deliverTotalMoney);
        response.setInvoiceMoney(invoiceMoney);
        return response;
    }

    /**
     * 组装分页响应数据
     */
    public static <T,R> PageMoneyResponse<R> wrap(Page<T> page, Function<List<T>, List<R>> callback, BigDecimal taxTotalMoney, BigDecimal noTaxTotalMoney, BigDecimal orderTotalMoney, BigDecimal buyTotalMoney, BigDecimal deliverTotalMoney) {
        PageMoneyResponse<R> response = new PageMoneyResponse<>();
        fillPageData(response, page, callback);
        response.setTaxTotalMoney(taxTotalMoney);
        response.setNoTaxTotalMoney(noTaxTotalMoney);
        response.setOrderTotalMoney(orderTotalMoney);
        response.setBuyTotalMoney(buyTotalMoney);
        response.setDeliverTotalMoney(deliverTotalMoney);
        return response;
    }

    /**
     * 组装分页响应数据
     */
    public static <T,R> PageMoneyResponse<R> wrap(Page<T> page, Function<List<T>, List<R>> callback, BigDecimal taxTotalMoney, BigDecimal noTaxTotalMoney, BigDecimal orderTotalMoney, BigDecimal buyTotalMoney) {
        PageMoneyResponse<R> response = new PageMoneyResponse<>();
        fillPageData(response, page, callback);
        response.setTaxTotalMoney(taxTotalMoney);
        response.setNoTaxTotalMoney(noTaxTotalMoney);
        response.setOrderTotalMoney(orderTotalMoney);
        response.setBuyTotalMoney(buyTotalMoney);
        return response;
    }

    /**
     * 组装分页响应数据
     */
    public static <T,R> PageMoneyResponse<R> wrap(Page<T> page, Function<List<T>, List<R>> callback, BigDecimal taxTotalMoney, BigDecimal buyTotalMoney, BigDecimal deliverTotalMoney) {
        PageMoneyResponse<R> response = new PageMoneyResponse<>();
        fillPageData(response, page, callback);
        response.setTaxTotalMoney(taxTotalMoney);
        response.setBuyTotalMoney(buyTotalMoney);
        response.setDeliverTotalMoney(deliverTotalMoney);
        return response;
    }

    /**
     * 组装分页响应数据
     */
    public static <T,R> PageMoneyResponse<R> wrap(Page<T> page, Function<List<T>, List<R>> callback, BigDecimal taxTotalMoney, BigDecimal noTaxTotalMoney) {
        PageMoneyResponse<R> response = new PageMoneyResponse<>();
        fillPageData(response, page, callback);
        response.setTaxTotalMoney(taxTotalMoney);
        response.setNoTaxTotalMoney(noTaxTotalMoney);
        return response;
    }

    /**
     * 组装分页响应数据
     */
    public static <T,R> PageMoneyResponse<R> wrap(Page<T> page, Function<List<T>, List<R>> callback, BigDecimal taxTotalMoney) {
        PageMoneyResponse<R> response = new PageMoneyResponse<>();
        fillPageData(response, page, callback);
        response.setTaxTotalMoney(taxTotalMoney);
        return response;
    }

    /**
     * 填充翻译数据
     */
    private static <T,R> void fillPageData(PageResult<R> response, Page<T> page, Function<List<T>, List<R>> callback) {
        response.setPage(page.getPages());
        response.setTotal(page.getTotal());
        response.setPageSize(page.getSize());
        response.setCurrent(page.getCurrent());
        response.setList(callback.apply(page.getRecords()));
    }
}
