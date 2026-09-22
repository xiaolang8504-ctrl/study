package com.study.module.system.guardian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 家长代上传、等待学生确认的采集请求。 */
@Data
@TableName("guardian_assisted_capture")
public class GuardianAssistedCapture {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long relationId;
    private Long studentUserId;
    private Long guardianUserId;
    private String grade;
    private String subject;
    private String questionType;
    private String source;
    private String learningPoint;
    private String errorLabels;
    private String sourceFileIds;
    /** 0 待学生确认，1 已确认并创建采集任务，2 已撤销。 */
    private Integer status;
    private Long confirmedByUserId;
    private Long captureTaskId;
    private LocalDateTime confirmTime;
    private LocalDateTime revokeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
