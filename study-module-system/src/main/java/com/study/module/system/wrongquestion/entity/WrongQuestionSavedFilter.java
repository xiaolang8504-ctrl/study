package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/** 学生保存的错题列表筛选条件。 */
@Data
public class WrongQuestionSavedFilter {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String filterName;
    private String filterJson;
    private Integer sortNo;
    private Integer defaultFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
