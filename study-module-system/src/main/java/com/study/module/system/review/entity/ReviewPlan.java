package com.study.module.system.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 用户错题复习计划实体
 */
@Data
public class ReviewPlan {

    /**
     * 复习计划ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 计划名称
     */
    private String planName;

    /**
     * 每日复习题量上限
     */
    private Integer dailyLimit;

    /**
     * 是否开启提醒: 0否, 1是
     */
    private Integer reminderEnabled;

    /**
     * 每日提醒时间
     */
    private LocalTime reminderTime;

    /**
     * 复习星期: 1周一至7周日，逗号分隔
     */
    private String reviewWeekDays;

    /**
     * 排期算法版本
     */
    private String algorithmVersion;

    /**
     * 计划状态: 0停用, 1启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
