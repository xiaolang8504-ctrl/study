package com.study.module.system.wrongquestion.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/** 错题文本规范化、指纹与相似度工具。 */
public final class WrongQuestionContentUtils {

    private WrongQuestionContentUtils() {
    }

    public static String normalized(String title, String content) {
        return normalized(title, content, null);
    }

    public static String normalized(String title, String content, String optionsJson) {
        String value = (StringUtils.hasText(title) ? title : "") + " "
                + (StringUtils.hasText(content) ? content : "") + " "
                + canonicalOptions(optionsJson);
        return value.replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ").replace("&amp;", "&")
                .toLowerCase().replaceAll("[\\p{P}\\p{Z}\\s]+", "").trim();
    }

    public static String fingerprint(String title, String content) {
        return fingerprint(title, content, null);
    }

    public static String fingerprint(String title, String content, String optionsJson) {
        String normalized = normalized(title, content, optionsJson);
        if (!StringUtils.hasText(normalized)) {
            return null;
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(normalized.getBytes(StandardCharsets.UTF_8));
            StringBuilder value = new StringBuilder(digest.length * 2);
            for (byte item : digest) {
                value.append(String.format("%02x", item & 0xff));
            }
            return value.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    public static int similarity(String left, String right) {
        if (!StringUtils.hasText(left) || !StringUtils.hasText(right)) {
            return 0;
        }
        if (left.equals(right)) {
            return 100;
        }
        Set<String> leftPairs = pairs(left);
        Set<String> rightPairs = pairs(right);
        int intersection = 0;
        for (String pair : leftPairs) {
            if (rightPairs.contains(pair)) {
                intersection++;
            }
        }
        return leftPairs.isEmpty() || rightPairs.isEmpty() ? 0
                : (int) Math.round(200D * intersection / (leftPairs.size() + rightPairs.size()));
    }

    private static Set<String> pairs(String value) {
        Set<String> result = new HashSet<>();
        if (value.length() == 1) {
            result.add(value);
            return result;
        }
        for (int index = 0; index < value.length() - 1; index++) {
            result.add(value.substring(index, index + 2));
        }
        return result;
    }

    private static String canonicalOptions(String optionsJson) {
        if (!StringUtils.hasText(optionsJson)) {
            return "";
        }
        try {
            JSONObject options = JSON.parseObject(optionsJson);
            return JSON.toJSONString(new TreeMap<>(options));
        } catch (RuntimeException ignored) {
            return optionsJson;
        }
    }
}
