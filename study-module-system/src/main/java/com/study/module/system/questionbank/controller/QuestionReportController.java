package com.study.module.system.questionbank.controller;

import com.study.common.core.domain.Result;
import com.study.common.core.domain.dto.PageResult;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.questionbank.dto.request.QuestionReportHandleReq;
import com.study.module.system.questionbank.dto.request.QuestionReportPageListReq;
import com.study.module.system.questionbank.dto.request.QuestionReportCreateReq;
import com.study.module.system.questionbank.dto.response.QuestionReportPageListResp;
import com.study.module.system.questionbank.service.QuestionReportCreateService;
import com.study.module.system.questionbank.service.QuestionReportHandleService;
import com.study.module.system.questionbank.service.QuestionReportListService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 题目举报接口
 */
@Api(tags = "题目举报")
@RestController
@RequestMapping("/api/questionReport")
public class QuestionReportController {

    @Autowired
    QuestionReportCreateService questionReportCreateService;

    @Autowired
    QuestionReportListService questionReportListService;

    @Autowired
    QuestionReportHandleService questionReportHandleService;

    @ApiOperation("举报题目")
    @PreAuthorize("hasAuthority('system:questionBank:reportQuestion')")
    /**
     * 执行 reportQuestion 业务处理。
     */
    @PostMapping("/reportQuestion")
    public Result<Void> reportQuestion(@RequestBody @Validated QuestionReportCreateReq request) {
        questionReportCreateService.reportQuestion(request);
        return ResultUtils.success();
    }

    @ApiOperation("题目举报分页列表")
    @PreAuthorize("hasAuthority('system:questionBank:questionReportPageList')")
    @GetMapping("/questionReportPageList")
    public Result<PageResult<QuestionReportPageListResp>> questionReportPageList(
            @Validated QuestionReportPageListReq request) {
        return ResultUtils.success(questionReportListService.questionReportPageList(request));
    }

    @ApiOperation("处理题目举报")
    @PreAuthorize("hasAuthority('system:questionBank:handleQuestionReport')")
    /**
     * 处理题目举报。
     */
    @PostMapping("/handleQuestionReport")
    public Result<Void> handleQuestionReport(@RequestBody @Validated QuestionReportHandleReq request) {
        questionReportHandleService.handleQuestionReport(request);
        return ResultUtils.success();
    }
}
