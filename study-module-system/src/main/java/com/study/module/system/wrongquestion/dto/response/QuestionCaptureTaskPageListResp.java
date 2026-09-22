package com.study.module.system.wrongquestion.dto.response;
import lombok.Data;
import java.time.LocalDateTime;
/**
 * 题目采集任务分页列表响应
 */
@Data
public class QuestionCaptureTaskPageListResp { private Long id; private String grade; private String subject; private Integer status; private String failReason; private Integer retryCount; private LocalDateTime createTime; }
