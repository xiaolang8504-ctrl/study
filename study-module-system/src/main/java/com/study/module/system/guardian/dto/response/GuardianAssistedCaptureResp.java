package com.study.module.system.guardian.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/** 代上传任务响应，不暴露原文件 URL 或题目内容。 */
@Data
public class GuardianAssistedCaptureResp {
    private Long id;
    private Long studentUserId;
    private String studentName;
    private String subject;
    private Integer status;
    private Long captureTaskId;
    private LocalDateTime createTime;
    private LocalDateTime confirmTime;
}
