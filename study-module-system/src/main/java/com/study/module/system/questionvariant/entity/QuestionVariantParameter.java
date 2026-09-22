package com.study.module.system.questionvariant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 参数变式题参数快照。
 */
@Data
@TableName("sys_question_variant_parameter")
public class QuestionVariantParameter {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 变式题记录ID
     */
    private Long variantRecordId;
    /**
     * 变量编码
     */
    private String variableCode;
    /**
     * 变量名称
     */
    private String variableName;
    /**
     * 文本值
     */
    private String valueText;
    /**
     * 数值
     */
    private BigDecimal numericValue;
    /**
     * 展示值
     */
    private String displayValue;
    /**
     * 单位
     */
    private String unit;
    /**
     * 生成来源
     */
    private String generateSource;
    /**
     * 排序号
     */
    private Integer sort;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
