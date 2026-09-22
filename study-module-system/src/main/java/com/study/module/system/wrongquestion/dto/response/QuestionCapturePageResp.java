package com.study.module.system.wrongquestion.dto.response;
import lombok.Data;
/**
 * 题目采集页面响应
 */
@Data
public class QuestionCapturePageResp { private Long id; private Long imageFileId; private Long cleanedFileId; private Long sourceFileId; private Integer sourcePageNo; private Integer pageNo; private Integer status; private String failReason; private Integer retryCount; private Integer cleanStatus; private String cleanFailReason; }
