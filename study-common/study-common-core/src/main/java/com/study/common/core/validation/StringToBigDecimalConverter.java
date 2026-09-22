package com.study.common.core.validation;

import com.fasterxml.jackson.databind.util.StdConverter;
import java.math.BigDecimal;

/**
 * 处理金额万千位符
 */
public class StringToBigDecimalConverter extends StdConverter<String, BigDecimal> {

    @Override
    public BigDecimal convert(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        // 移除千位分隔符逗号
        String cleanedValue = value.replace(",", "");
        return new BigDecimal(cleanedValue);
    }
}
