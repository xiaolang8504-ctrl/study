package com.study.module.system.wrongquestion.dto.request;
import lombok.Data;
import javax.validation.constraints.NotNull;
/**
 * 题目采集题块标识请求
 */
@Data
public class QuestionCaptureRegionIdReq { @NotNull(message="题块ID不能为空") private Long id; }
