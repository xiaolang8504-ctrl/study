package com.study.module.system.questionvariant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 参数变式题计算过程。
 */
@Data
@TableName("sys_question_variant_calculation")
public class QuestionVariantCalculation {
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
     * 公式ID
     */
    private Long formulaId;
    /**
     * 公式编码
     */
    private String formulaCode;
    /**
     * 计算步骤序号
     */
    private Integer stepNo;
    /**
     * 表达式
     */
    private String expression;
    /**
     * 输入参数JSON
     */
    private String inputJson;
    /**
     * 原始结果
     */
    private String rawResult;
    /**
     * 数值结果
     */
    private BigDecimal numericResult;
    /**
     * 格式化结果
     */
    private String formattedResult;
    /**
     * 结果单位
     */
    private String resultUnit;
    /**
     * 执行状态
     */
    private String executeStatus;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 执行耗时，毫秒
     */
    private Integer durationMs;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
