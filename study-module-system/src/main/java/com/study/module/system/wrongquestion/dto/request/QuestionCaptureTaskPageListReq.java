package com.study.module.system.wrongquestion.dto.request;
import com.study.common.core.domain.dto.PageParam;
import lombok.Data;
/**
 * 题目采集任务分页查询请求
 */
@Data
public class QuestionCaptureTaskPageListReq extends PageParam { private Integer status; }
