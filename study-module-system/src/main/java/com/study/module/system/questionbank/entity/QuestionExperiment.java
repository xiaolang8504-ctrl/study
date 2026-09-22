package com.study.module.system.questionbank.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 相似题推荐A/B实验配置。
 */
@Data
@TableName("sys_question_experiment")
public class QuestionExperiment {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 实验名称
     */
    private String experimentName;
    /**
     * 启用状态
     */
    private Integer enable;
    /**
     * A组流量占比
     */
    private Integer groupATraffic;
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建人ID
     */
    private Long createId;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
