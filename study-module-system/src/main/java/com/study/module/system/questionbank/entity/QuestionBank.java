package com.study.module.system.questionbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审核题库实体。
 */
@Data
@TableName("sys_question_bank")
public class QuestionBank {
    /**
     * 题库题目ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 年级字典键值。
     */
    private String grade;

    /**
     * 年级名称。
     */
    private String gradeName;

    /**
     * 科目字典键值。
     */
    private String subject;

    /**
     * 科目名称。
     */
    private String subjectName;

    /**
     * 题型字典键值。
     */
    private String questionType;

    /**
     * 题型名称。
     */
    private String questionTypeName;

    /**
     * 题目标题。
     */
    private String questionTitle;

    /**
     * 题干内容。
     */
    private String questionContent;

    /**
     * 规范化题干MD5，用于重复题检测。
     */
    private String questionHash;

    /**
     * 内容格式：TEXT普通文本，LATEX公式。
     */
    private String contentFormat;

    /**
     * 题目图片URL，多个使用逗号分隔。
     */
    private String imageUrls;

    /**
     * 选择题选项JSON。
     */
    private String optionsJson;

    /**
     * 标准答案。
     */
    private String correctAnswer;

    /**
     * 判题模式：AUTO客观题自动判定，SELF主观题查看答案后自评。
     */
    private String judgeMode;

    /**
     * 题目解析。
     */
    private String analysis;

    /**
     * 难度等级：1简单至5困难。
     */
    private Integer difficulty;

    /**
     * 题目来源字典键值。
     */
    private String source;

    /**
     * 题目来源名称。
     */
    private String sourceName;

    /** 内容提供方或版权方。 */
    private String provider;

    /** 外部系统或合同中的题目唯一标识。 */
    private String externalId;

    /** 授权说明、合同编号或适用范围。 */
    private String license;

    /** 授权到期日期；为空表示未设置到期日。 */
    private java.time.LocalDate expireAt;

    /**
     * 审核状态：0待审核，1通过，2驳回。
     */
    private Integer reviewStatus;

    /**
     * 审核备注或驳回原因。
     */
    private String reviewRemark;

    /**
     * 审核人用户ID。
     */
    private Long reviewerId;

    /**
     * 审核时间。
     */
    private LocalDateTime reviewTime;

    /**
     * 启用状态：0停用，1启用。
     */
    private Integer enable;

    /**
     * 创建人用户ID。
     */
    private Long createId;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
