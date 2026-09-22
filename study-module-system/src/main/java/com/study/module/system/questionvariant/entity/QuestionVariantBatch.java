package com.study.module.system.questionvariant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 参数变式生成批次。
 */
@Data
@TableName("sys_question_variant_batch")
public class QuestionVariantBatch {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 批次号
     */
    private String batchNo;
    /**
     * 模板ID
     */
    private Long templateId;
    /**
     * 模板版本号
     */
    private Integer templateVersion;
    /**
     * 生成模式
     */
    private String generateMode;
    /**
     * 请求生成数量
     */
    private Integer requestCount;
    /**
     * 已处理数量
     */
    private Integer processedCount;
    /**
     * 成功数量
     */
    private Integer successCount;
    /**
     * 失败数量
     */
    private Integer failureCount;
    /**
     * 状态
     */
    private String status;
    /**
     * 配置JSON
     */
    private String configJson;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 创建人ID
     */
    private Long createId;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
