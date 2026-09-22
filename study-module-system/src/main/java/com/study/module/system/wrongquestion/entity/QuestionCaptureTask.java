package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 学生题目采集任务。
 */
@Data
public class QuestionCaptureTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String grade;
    private String subject;
    private String questionType;
    private String source;
    private String learningPoint;
    private String errorLabels;
    /**
     * 客户端创建请求标识，用于网络重试时避免重复创建采集任务。
     */
    private String clientRequestId;
    /**
     * 0待处理、1处理中、2待确认、3已完成、4失败
     */
    private Integer status;
    private String failReason;
    private Integer retryCount;
    private Long createId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
