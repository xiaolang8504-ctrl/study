package com.study.module.system.wrongquestion.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 题目指纹与相似度规则测试。 */
class WrongQuestionContentUtilsTest {

    @Test
    void shouldIgnoreRichTextPunctuationAndWhitespaceWhenFingerprinting() {
        String richText = WrongQuestionContentUtils.fingerprint(" 一元方程 ",
                "<p>求解：x + 1 = 2。</p>");
        String plainText = WrongQuestionContentUtils.fingerprint("一元方程",
                "求解 x+1=2");

        assertEquals(plainText, richText);
        assertNull(WrongQuestionContentUtils.fingerprint(" ", "<p></p>"));
    }

    @Test
    void shouldDistinguishExactSimilarAndDifferentQuestions() {
        String source = WrongQuestionContentUtils.normalized("勾股定理", "直角边为3和4，求斜边");
        String similar = WrongQuestionContentUtils.normalized("勾股定理应用", "直角边为3、4，求斜边长度");
        String different = WrongQuestionContentUtils.normalized("一次函数", "求 y=2x+1 的图像");

        assertEquals(100, WrongQuestionContentUtils.similarity(source, source));
        assertTrue(WrongQuestionContentUtils.similarity(source, similar)
                > WrongQuestionContentUtils.similarity(source, different));
        assertNotEquals(WrongQuestionContentUtils.fingerprint("A", "1"),
                WrongQuestionContentUtils.fingerprint("B", "2"));
        assertNotEquals(WrongQuestionContentUtils.fingerprint("选择题", "下列正确的是", "{\"A\":\"1\"}"),
                WrongQuestionContentUtils.fingerprint("选择题", "下列正确的是", "{\"A\":\"2\"}"));
        assertEquals(WrongQuestionContentUtils.fingerprint("选择题", "下列正确的是", "{\"B\":\"2\",\"A\":\"1\"}"),
                WrongQuestionContentUtils.fingerprint("选择题", "下列正确的是", "{\"A\":\"1\",\"B\":\"2\"}"));
    }
}
