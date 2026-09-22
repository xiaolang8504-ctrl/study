package com.study.module.system.wrongquestion.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 题目采集页面标识请求。
 */
@Data
public class QuestionCapturePageIdReq {

    @NotNull(message = "采集页面ID不能为空")
    private Long id;
}
