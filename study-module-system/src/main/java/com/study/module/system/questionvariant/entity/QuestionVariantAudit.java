package com.study.module.system.questionvariant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参数变式题人工审核记录。
 */
@Data
@TableName("sys_question_variant_audit")
public class QuestionVariantAudit {
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
     * 审核动作
     */
    private String auditAction;
    /**
     * 审核状态
     */
    private Integer auditStatus;
    /**
     * 变更前快照
     */
    private String beforeSnapshot;
    /**
     * 变更后快照
     */
    private String afterSnapshot;
    /**
     * 审核备注
     */
    private String auditRemark;
    /**
     * 审核人ID
     */
    private Long auditorId;
    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
}
