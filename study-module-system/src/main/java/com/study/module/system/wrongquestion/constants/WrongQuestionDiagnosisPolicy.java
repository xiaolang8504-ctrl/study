package com.study.module.system.wrongquestion.constants;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 错题诊断的稳定词表与兼容转换规则。
 *
 * <p>错误标签历史上是自由文本。本策略新增稳定编码，但仍保留可读标签和学生自定义补充，
 * 使历史记录、报告下钻与新的结构化统计能同时工作。</p>
 */
public final class WrongQuestionDiagnosisPolicy {

    public static final String CAUSE_READING = "READING";
    public static final String CAUSE_CONCEPT = "CONCEPT";
    public static final String CAUSE_METHOD = "METHOD";
    public static final String CAUSE_CALCULATION = "CALCULATION";
    public static final String CAUSE_EXPRESSION = "EXPRESSION";

    public static final String ABILITY_FOUNDATION = "FOUNDATION";
    public static final String ABILITY_APPLICATION = "APPLICATION";
    public static final String ABILITY_COMPREHENSIVE = "COMPREHENSIVE";

    private static final Map<String, String> CAUSE_LABELS;
    private static final Map<String, String> ABILITY_LABELS;

    static {
        Map<String, String> causeLabels = new LinkedHashMap<>();
        causeLabels.put(CAUSE_READING, "审题");
        causeLabels.put(CAUSE_CONCEPT, "概念");
        causeLabels.put(CAUSE_METHOD, "方法");
        causeLabels.put(CAUSE_CALCULATION, "计算");
        causeLabels.put(CAUSE_EXPRESSION, "表达");
        CAUSE_LABELS = Collections.unmodifiableMap(causeLabels);

        Map<String, String> abilityLabels = new LinkedHashMap<>();
        abilityLabels.put(ABILITY_FOUNDATION, "基础");
        abilityLabels.put(ABILITY_APPLICATION, "应用");
        abilityLabels.put(ABILITY_COMPREHENSIVE, "综合");
        ABILITY_LABELS = Collections.unmodifiableMap(abilityLabels);
    }

    private WrongQuestionDiagnosisPolicy() {
    }

    public static Map<String, String> causeLabels() {
        return CAUSE_LABELS;
    }

    public static Map<String, String> abilityLabels() {
        return ABILITY_LABELS;
    }

    public static String normalizeCauseCodes(String errorCauseCodes, String errorLabels) {
        Set<String> codes = new LinkedHashSet<>(split(errorCauseCodes));
        for (String label : split(errorLabels)) {
            CAUSE_LABELS.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(label))
                    .map(Map.Entry::getKey)
                    .forEach(codes::add);
        }
        return codes.stream().filter(CAUSE_LABELS::containsKey).collect(Collectors.joining(","));
    }

    public static String normalizeErrorLabels(String errorLabels, String errorCauseCodes) {
        LinkedHashSet<String> labels = new LinkedHashSet<>();
        for (String code : split(errorCauseCodes)) {
            String label = CAUSE_LABELS.get(code);
            if (label != null) {
                labels.add(label);
            }
        }
        labels.addAll(split(errorLabels));
        return String.join(",", labels);
    }

    public static List<String> causeCodes(String errorCauseCodes, String errorLabels) {
        String normalized = normalizeCauseCodes(errorCauseCodes, errorLabels);
        return split(normalized);
    }

    public static String causeLabel(String causeCode) {
        return CAUSE_LABELS.get(causeCode);
    }

    public static String abilityLabel(String abilityLevel) {
        return ABILITY_LABELS.get(abilityLevel);
    }

    public static boolean isAbilityLevel(String abilityLevel) {
        return !StringUtils.hasText(abilityLevel) || ABILITY_LABELS.containsKey(abilityLevel);
    }

    private static List<String> split(String value) {
        if (!StringUtils.hasText(value)) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split("[,，]"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), ArrayList::new));
    }
}
