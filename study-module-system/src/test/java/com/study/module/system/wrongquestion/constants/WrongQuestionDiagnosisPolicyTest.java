package com.study.module.system.wrongquestion.constants;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * F2-02 的稳定错因与能力层级兼容规则测试。
 */
class WrongQuestionDiagnosisPolicyTest {

    @Test
    void shouldNormalizeStructuredCausesAndKeepCustomLabels() {
        String causeCodes = WrongQuestionDiagnosisPolicy.normalizeCauseCodes(
                "CALCULATION,READING,CALCULATION", "概念,单位遗漏");

        assertEquals("CALCULATION,READING,CONCEPT", causeCodes);
        assertEquals("计算,审题,概念,单位遗漏", WrongQuestionDiagnosisPolicy.normalizeErrorLabels(
                "概念,单位遗漏", causeCodes));
        assertEquals(Arrays.asList("CALCULATION", "CONCEPT"),
                WrongQuestionDiagnosisPolicy.causeCodes(null, "计算,概念,自定义错因"));
    }

    @Test
    void shouldOnlyAcceptKnownAbilityLevels() {
        assertTrue(WrongQuestionDiagnosisPolicy.isAbilityLevel("FOUNDATION"));
        assertTrue(WrongQuestionDiagnosisPolicy.isAbilityLevel(""));
        assertEquals("综合", WrongQuestionDiagnosisPolicy.abilityLabel("COMPREHENSIVE"));
    }
}
