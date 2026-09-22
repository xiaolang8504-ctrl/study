package com.study.module.system.questionvariant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 参数变式变量配置。
 */
@Data
@TableName("sys_question_variant_variable")
public class QuestionVariantVariable {
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
     * 变量编码
     */
    private String variableCode;
    /**
     * 变量名称
     */
    private String variableName;
    /**
     * 值类型
     */
    private String valueType;
    /**
     * 最小值
     */
    private BigDecimal minValue;
    /**
     * 最大值
     */
    private BigDecimal maxValue;
    /**
     * 步长值
     */
    private BigDecimal stepValue;
    /**
     * 小数位数
     */
    private Integer decimalScale;
    /**
     * 枚举值JSON
     */
    private String enumValuesJson;
    /**
     * 单位类型
     */
    private String unitType;
    /**
     * 单位
     */
    private String unit;
    /**
     * 是否必填
     */
    private Integer isRequired;
    /**
     * 排序号
     */
    private Integer sort;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
