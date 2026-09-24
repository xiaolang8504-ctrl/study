package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/** 学生维护的错题个人标签。 */
@Data
public class WrongQuestionTag {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String tagName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
