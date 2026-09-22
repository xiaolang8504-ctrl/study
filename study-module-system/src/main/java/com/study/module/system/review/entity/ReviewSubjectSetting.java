package com.study.module.system.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户复习计划科目设置实体
 */
@Data
public class ReviewSubjectSetting {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 复习计划ID
     */
    private Long planId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 科目字典键值
     */
    private String subject;

    /**
     * 科目名称
     */
    private String subjectName;

    /**
     * 是否参与智能复习: 0否, 1是
     */
    private Integer enabled;

    /**
     * 该科目每日复习题量上限
     */
    private Integer dailyLimit;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
