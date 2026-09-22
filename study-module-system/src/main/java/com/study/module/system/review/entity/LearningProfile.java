package com.study.module.system.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学生学习偏好实体。
 */
@Data
@TableName("sys_learning_profile")
public class LearningProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 当前登录用户 ID。 */
    private Long userId;

    /** 年级字典键值。 */
    private String grade;

    /** 科目字典键值。 */
    private String subject;

    /** 已选教材 ID。 */
    private Long bookId;

    /** 教材标题快照。 */
    private String bookTitle;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
