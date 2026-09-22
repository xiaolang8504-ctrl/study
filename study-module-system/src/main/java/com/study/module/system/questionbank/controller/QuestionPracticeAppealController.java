package com.study.module.system.questionbank.controller;

import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.questionbank.dto.request.QuestionPracticeAppealCreateReq;
import com.study.module.system.questionbank.dto.request.QuestionPracticeAppealReviewReq;
import com.study.module.system.questionbank.dto.response.QuestionPracticeAppealListResp;
import com.study.module.system.questionbank.service.QuestionPracticeAppealService;
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

import java.util.List;

/**
 * 主观题作答申诉前端控制器
 */
@Api(tags = "主观题作答申诉")
@RestController
@RequestMapping("/api/questionPracticeAppeal")
public class QuestionPracticeAppealController {

    @Autowired
    private QuestionPracticeAppealService questionPracticeAppealService;

    /**
     * 提交主观题作答申诉。
     */
    @ApiOperation("提交主观题作答申诉")
    @PreAuthorize("hasAuthority('system:questionBank:createQuestionPracticeAppeal')")
    @PostMapping("/createQuestionPracticeAppeal")
    public Result<Void> createQuestionPracticeAppeal(@RequestBody @Validated QuestionPracticeAppealCreateReq request) {
        questionPracticeAppealService.createQuestionPracticeAppeal(request);
        return ResultUtils.success();
    }

    /**
     * 查询主观题作答申诉列表。
     */
    @ApiOperation("主观题作答申诉列表")
    @PreAuthorize("hasAuthority('system:questionBank:questionPracticeAppealList')")
    @GetMapping("/questionPracticeAppealList")
    public Result<List<QuestionPracticeAppealListResp>> questionPracticeAppealList(Integer status) {
        return ResultUtils.success(questionPracticeAppealService.questionPracticeAppealList(status));
    }

    /**
     * 审核主观题作答申诉。
     */
    @ApiOperation("教师复核主观题作答申诉")
    @PreAuthorize("hasAuthority('system:questionBank:reviewQuestionPracticeAppeal')")
    @PostMapping("/reviewQuestionPracticeAppeal")
    public Result<Void> reviewQuestionPracticeAppeal(@RequestBody @Validated QuestionPracticeAppealReviewReq request) {
        questionPracticeAppealService.reviewQuestionPracticeAppeal(request);
        return ResultUtils.success();
    }
}
