package com.study.module.system.questionvariant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参数变式模板版本快照。
 */
@Data
@TableName("sys_question_variant_template_version")
public class QuestionVariantTemplateVersion {
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
     * 快照JSON
     */
    private String snapshotJson;
    /**
     * 变更摘要
     */
    private String changeSummary;
    /**
     * 版本状态
     */
    private Integer versionStatus;
    /**
     * 操作人ID
     */
    private Long operatorId;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
