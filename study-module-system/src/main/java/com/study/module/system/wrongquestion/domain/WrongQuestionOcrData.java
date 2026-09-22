package com.study.module.system.wrongquestion.domain;

import lombok.Data;

/**
 * OCR识别出的错题数据
 */
@Data
public class WrongQuestionOcrData {

    /**
     * 题目类型
     */
    private String questionType;

    /**
     * 题目标题
     */
    private String questionTitle;

    /**
     * 题目内容
     */
    private String questionContent;

    /**
     * 错误答案
     */
    private String wrongAnswer;

    /**
     * 正确答案
     */
    private String correctAnswer;

    /**
     * 错误原因
     */
    private String wrongReason;

    /**
     * 题目解析
     */
    private String analysis;
    /**
     * 归一化题块坐标，0-10000。
     */
    private Integer leftPosition;
    private Integer topPosition;
    private Integer width;
    private Integer height;
    /**
     * OCR版面检测置信度，0-100。
     */
    private Integer confidence;
}
