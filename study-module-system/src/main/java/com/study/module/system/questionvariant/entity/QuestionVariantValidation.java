package com.study.module.system.questionvariant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参数变式题校验明细。
 */
@Data
@TableName("sys_question_variant_validation")
public class QuestionVariantValidation {
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
     * 校验编码
     */
    private String validationCode;
    /**
     * 校验类型
     */
    private String validationType;
    /**
     * 校验级别
     */
    private String validationLevel;
    /**
     * 校验状态
     */
    private String validationStatus;
    /**
     * 输入快照
     */
    private String inputSnapshot;
    /**
     * 提示信息
     */
    private String message;
    /**
     * 执行顺序
     */
    private Integer executionOrder;
    /**
     * 执行耗时，毫秒
     */
    private Integer durationMs;
    /**
     * 校验时间
     */
    private LocalDateTime validateTime;
}
