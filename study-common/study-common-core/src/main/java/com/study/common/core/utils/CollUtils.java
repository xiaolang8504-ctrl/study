package com.study.common.core.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 集合工具
 */
public class CollUtils {

    private static final String[] nums = {"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private static final String[] bigNums = {"", "十", "百", "千", "万", "亿"};

    /**
     * 数字字符串转换成集合
     */
    public static List<Integer> idsToList(String ids) {
        return Arrays.stream(ids.split(","))
                .filter(s -> !s.isEmpty()).map(Integer::parseInt).collect(Collectors.toList());
    }

    /**
     * 数字字符串转换成集合
     */
    public static List<Long> idsToLongList(String ids) {
        return Arrays.stream(ids.split(","))
                .filter(s -> !s.isEmpty()).map(Long::parseLong).collect(Collectors.toList());
    }

    /**
     * 将 BigDecimal 格式化为财务字符串
     * - 使用千位分隔符（逗号）
     * - 保留两位小数（不足补零）
     */
    public static String formatToFinancial(BigDecimal value) {
        if (value == null) {
            return "";
        }

        // 创建财务专用格式
        DecimalFormat df = new DecimalFormat("#,##0.00");

        // 强制使用美式数字格式（确保分隔符正确）
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.US);
        df.setDecimalFormatSymbols(symbols);

        return df.format(value);
    }

    /**
     * 数字字符串转换成集合
     */
    public static Integer converPercentage(BigDecimal num,BigDecimal total) {
        if (num.compareTo(BigDecimal.ZERO) == 0 || total.compareTo(BigDecimal.ZERO) == 0){
            return 0;
        }
        BigDecimal result = num.divide(total, 4, RoundingMode.HALF_UP); // 先进行除法并四舍五入到小数点后10位
        result = result.multiply(new BigDecimal("100"));
        result = result.setScale(0, RoundingMode.HALF_UP);
        return result.intValue();
    }

    /**
     * 数字转换成汉字数字
     */
    public static String numberToChinese(int number) {
        if (number == 0) {
            return "零";
        }

        StringBuilder chinese = new StringBuilder();
        boolean zero = false; // 用于标记是否需要插入“零”
        int unit = 0; // 用于跟踪当前的单位（十、百、千等）

        while (number > 0) {
            int part = number % 10; // 获取当前最低位的数字
            if (part == 0) {
                if (!zero) {
                    chinese.insert(0, nums[part]); // 插入“零”
                    zero = true; // 设置标记，表示已经有零插入
                }
            } else {
                chinese.insert(0, nums[part] + bigNums[unit]); // 插入数字和单位
                zero = false; // 重置零的标记
            }
            number /= 10; // 移除已处理的最低位
            unit++; // 移动到下一个单位（十、百、千等）
        }
        return chinese.toString();
    }

    /**
     * 数字四舍五入保留两位小数,不够补00
     */
    public static String setScale(BigDecimal num) {
        if (num == null) {
            return "0.00";
        }
        // 步骤2: 四舍五入到两位小数
        BigDecimal roundedValue = num.setScale(2, RoundingMode.HALF_UP);

        // 步骤3: 转换为字符串，并补足到至少包含两位小数
        String formattedValue = String.format("%.2f", roundedValue.doubleValue());

        // 如果小数部分不足两位，则补足"00"
        if (formattedValue.indexOf('.') == -1) {
            formattedValue += ".00";
        } else if (formattedValue.split("\\.")[1].length() == 1) {
            formattedValue += "0";
        }
        return formattedValue;
    }

    /**
     * 数字以万为单位四舍五入保留两位小数,不够补00
     */
    public static String setScaleRounding(BigDecimal num) {
        if (num == null) {
            return "0.00";
        }
        BigDecimal valueInWan = num.divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP);
        return setScale(valueInWan);
    }

    /**
     * 获取指定位数的随机数
     */
    public static String roundingStr(Integer num) {
        Random random = new Random();
        int randomNum = random.nextInt(1000000000);
        String randomStr = String.valueOf(randomNum);
        String str = randomStr.substring(0, num);
        return str;
    }

    /**
     * 获取12位数的数字字符串特除符号的字符串
     */
    public static String roundingPassWordStr() {
        String lowercase = "abcdefghijklmnpqrstuvwxyz"; // 小写字母 (去掉了易混淆的o)
        String uppercase = "ABCDEFGHIJKLMNPQRSTUVWXYZ"; // 大写字母 (去掉了易混淆的O)
        String digits = "0123456789"; // 数字
        String specialChars = "@$!%*#~?&"; // 特殊字符

        // 使用更安全的随机数生成器
        SecureRandom random = new SecureRandom();

        // 确保密码包含至少一个每种类型的字符
        List<Character> passwordChars = new ArrayList<>();

        // 添加至少一个小写字母
        passwordChars.add(lowercase.charAt(random.nextInt(lowercase.length())));

        // 添加至少一个大写字母
        passwordChars.add(uppercase.charAt(random.nextInt(uppercase.length())));

        // 添加至少一个数字
        passwordChars.add(digits.charAt(random.nextInt(digits.length())));

        // 添加至少一个特殊字符
        passwordChars.add(specialChars.charAt(random.nextInt(specialChars.length())));

        // 组合所有可用字符
        String allChars = lowercase + uppercase + digits + specialChars;

        // 填充剩余的8个字符
        for (int i = 0; i < 8; i++) {
            passwordChars.add(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 随机打乱字符顺序
        Collections.shuffle(passwordChars, random);

        // 构建最终密码字符串
        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }
}
