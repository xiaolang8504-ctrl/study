package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 同一道题的一次错误及其原卷定位快照。 */
@Data
@TableName("sys_wrong_question_occurrence")
public class WrongQuestionOccurrence {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long canonicalQuestionId;
    private Long originWrongQuestionId;
    private Long userId;
    private String wrongAnswer;
    private String source;
    private String sourceName;
    private Long captureTaskId;
    private Long capturePageId;
    private Long captureRegionId;
    private Integer captureSourcePageNo;
    private Integer leftPosition;
    private Integer topPosition;
    private Integer width;
    private Integer height;
    private LocalDateTime occurredAt;
    private LocalDateTime createTime;
}
