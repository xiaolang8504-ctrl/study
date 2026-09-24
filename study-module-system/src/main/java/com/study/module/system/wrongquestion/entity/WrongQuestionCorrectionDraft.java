package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 学生尚未提交的订正草稿。 */
@Data
@TableName("sys_wrong_question_correction_draft")
public class WrongQuestionCorrectionDraft {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long wrongQuestionId;
    private Long userId;
    private String thinking;
    private String errorReason;
    private String correctionAnswer;
    private String correctionAnalysis;
    private String correctionImageUrl;
    private String correctionRemark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
