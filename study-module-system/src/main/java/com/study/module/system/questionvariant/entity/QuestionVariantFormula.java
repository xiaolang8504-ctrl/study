package com.study.module.system.questionvariant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参数变式计算公式。
 */
@Data
@TableName("sys_question_variant_formula")
public class QuestionVariantFormula {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 模板ID
     */
    private Long templateId;
    /**
     * 版本号
     */
    private Integer versionNo;
    /**
     * 公式编码
     */
    private String formulaCode;
    /**
     * 公式名称
     */
    private String formulaName;
    /**
     * 表达式
     */
    private String expression;
    /**
     * 目标变量
     */
    private String targetVariable;
    /**
     * 执行顺序
     */
    private Integer executionOrder;
    /**
     * 精度位数
     */
    private Integer precisionScale;
    /**
     * 舍入模式
     */
    private String roundingMode;
    /**
     * 启用状态
     */
    private Integer enable;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
