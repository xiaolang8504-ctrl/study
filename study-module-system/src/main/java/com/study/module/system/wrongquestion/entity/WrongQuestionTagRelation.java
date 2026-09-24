package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/** 错题与个人标签的结构化关联。 */
@Data
public class WrongQuestionTagRelation {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long wrongQuestionId;
    private Long tagId;
    private Long userId;
    private LocalDateTime createTime;
}
