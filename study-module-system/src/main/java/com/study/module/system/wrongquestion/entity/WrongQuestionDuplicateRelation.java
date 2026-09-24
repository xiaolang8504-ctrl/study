package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 错题重复/相似关系及合并状态。 */
@Data
@TableName("sys_wrong_question_duplicate_relation")
public class WrongQuestionDuplicateRelation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long leftQuestionId;
    private Long rightQuestionId;
    private String matchType;
    private Integer similarityScore;
    private String status;
    private Long keptQuestionId;
    private Long mergedQuestionId;
    private Integer mergedBeforeStatus;
    private Long createId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
