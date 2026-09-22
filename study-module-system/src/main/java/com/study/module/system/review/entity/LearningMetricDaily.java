package com.study.module.system.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学生学习日快照。
 */
@Data
@TableName("sys_learning_metric_daily")
public class LearningMetricDaily {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 快照日期。 */
    private LocalDate metricDate;
    /** 学生用户ID。 */
    private Long userId;
    /** 科目字典键值，ALL 表示全部科目汇总。 */
    private String subject;
    private Integer wrongQuestionCount;
    private Integer pendingCorrectionCount;
    private Integer correctedCount;
    private Integer masteredCount;
    private Integer archivedCount;
    private Integer masteryRate;
    private Integer reviewCount;
    private Integer judgedAnswerCount;
    private Integer correctAnswerCount;
    private Integer retentionRate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
