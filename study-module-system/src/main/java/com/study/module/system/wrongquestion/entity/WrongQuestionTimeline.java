package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 错题学习证据时间线。
 */
@Data
@TableName("sys_wrong_question_timeline")
public class WrongQuestionTimeline {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long wrongQuestionId;
    private String eventType;
    private String eventSource;
    private String eventContent;
    private Long createId;
    private LocalDateTime createTime;
}
