package com.study.common.core.utils;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * 时间日期工具
 */
public class TimeDateUtils {
    /**
     * 获取两个时间之间的月份列表
     */
    public static List<Integer> generateMonthRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("起始日期不能晚于结束日期");
        }
        List<Integer> months = new ArrayList<>();
        YearMonth current = YearMonth.from(startDate);
        YearMonth last = YearMonth.from(endDate);

        while (!current.isAfter(last)) {
            // 获取月份值 (1-12)
            months.add(current.getMonthValue());
            current = current.plusMonths(1);
        }
        return months;
    }

    /**
     * 开始时间(本年)
     */
    public static LocalDateTime startOfYear(){
        return LocalDate.now().withDayOfYear(1).atStartOfDay();
    }

    /**
     * 开始时间(本季)
     */
    public static LocalDateTime startOfSeason(){
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
        return today.withMonth(quarterStartMonth).withDayOfMonth(1).atStartOfDay();
    }

    /**
     * 结束时间(本季)
     */
    public static LocalDateTime endOfSeason(){
        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int quarterEndMonth = ((currentMonth - 1) / 3 + 1) * 3;
        return today
                .withMonth(quarterEndMonth)
                .with(TemporalAdjusters.lastDayOfMonth())
                .atTime(LocalTime.MAX);
    }

    /**
     * 开始时间(本月)
     */
    public static LocalDateTime startOfMonth(){
        return LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();
    }

    /**
     * 结束时间(本月)
     */
    public static LocalDateTime endOfMonth(){
        return LocalDate.now()
                .with(TemporalAdjusters.lastDayOfMonth())
                .atTime(LocalTime.MAX);
    }

    /**
     * 开始时间(本周)
     */
    public static LocalDateTime startOfWeek(){
        return LocalDate.now()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .atStartOfDay();
    }

    /**
     * 结束时间(本周)
     */
    public static LocalDateTime endOfWeek(){
        return LocalDate.now()
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .atTime(LocalTime.MAX);
    }

    /**
     * 当前时间(去年)
     */
    public static LocalDateTime currentEndOfLastYear(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        // 直接减去一年
        return currentDateTime.minusYears(1);
    }

    /**
     * 开始时间(去年)
     */
    public static LocalDateTime startOfLastYear(Integer lastYear){
        return LocalDate.of(lastYear, 1, 1).atStartOfDay();
    }

    /**
     * 结束时间(去年)
     */
    public static LocalDateTime endOfLastYear(Integer lastYear){
        return LocalDate.of(lastYear, 12, 31).atTime(23, 59, 59);
    }
}
