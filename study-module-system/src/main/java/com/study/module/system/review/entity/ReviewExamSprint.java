package com.study.module.system.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 考前冲刺约束；不改写常规复习队列。 */
@Data
@TableName("sys_review_exam_sprint")
public class ReviewExamSprint {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String subject;
    private LocalDate examDate;
    private String scopeText;
    private Integer dailyMinutes;
    private Integer targetQuestionCount;
    /** 0关闭，1启用。 */
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
