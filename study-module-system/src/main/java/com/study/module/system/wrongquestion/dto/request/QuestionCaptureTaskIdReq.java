package com.study.module.system.wrongquestion.dto.request;
import lombok.Data;
import org.hibernate.validator.constraints.Range;
/**
 * 题目采集任务标识请求
 */
@Data
public class QuestionCaptureTaskIdReq { @Range(min = 1, message = "采集任务ID需大于{min}") private Long id; }
