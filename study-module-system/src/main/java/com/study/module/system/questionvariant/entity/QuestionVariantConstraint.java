package com.study.module.system.questionvariant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参数变式约束规则。
 */
@Data
@TableName("sys_question_variant_constraint")
public class QuestionVariantConstraint {
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
     * 约束编码
     */
    private String constraintCode;
    /**
     * 约束类型
     */
    private String constraintType;
    /**
     * 表达式
     */
    private String expression;
    /**
     * 约束级别
     */
    private String constraintLevel;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 执行顺序
     */
    private Integer executionOrder;
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
