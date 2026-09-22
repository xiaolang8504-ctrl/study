package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 题目采集关键事件审计记录。
 */
@Data
public class QuestionCaptureEvent {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long pageId;
    private Long regionId;
    private Long userId;
    private Long fileId;
    private String eventType;
    private String result;
    private String ocrProvider;
    private String ocrModel;
    private Integer regionCount;
    private Long elapsedMillis;
    private String errorMessage;
    private LocalDateTime createTime;
}
