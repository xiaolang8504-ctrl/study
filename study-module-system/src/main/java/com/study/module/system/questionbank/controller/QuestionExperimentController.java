package com.study.module.system.questionbank.controller;

import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.questionbank.dto.request.QuestionExperimentSaveReq;
import com.study.module.system.questionbank.dto.response.QuestionExperimentHistoryResp;
import com.study.module.system.questionbank.dto.response.QuestionExperimentResp;
import com.study.module.system.questionbank.service.QuestionExperimentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 相似题 A/B 实验前端控制器
 */
@Api(tags = "相似题A/B实验")
@RestController
@RequestMapping("/api/questionExperiment")
public class QuestionExperimentController {
    @Autowired
    QuestionExperimentService questionExperimentService;

    /**
     * 查询相似题 A/B 实验详情。
     */
    @ApiOperation("A/B实验配置详情")
    @PreAuthorize("hasAuthority('system:questionBank:questionExperimentDetail')")
    @GetMapping("/questionExperimentDetail")
    public Result<QuestionExperimentResp> questionExperimentDetail() {
        return ResultUtils.success(questionExperimentService.questionExperimentDetail());
    }

    /**
     * 创建或保存相似题 A/B 实验。
     */
    @ApiOperation("保存A/B实验配置")
    @PreAuthorize("hasAuthority('system:questionBank:saveQuestionExperiment')")
    @PostMapping("/saveQuestionExperiment")
    public Result<Void> saveQuestionExperiment(@RequestBody @Validated QuestionExperimentSaveReq request) {
        questionExperimentService.saveQuestionExperiment(request);
        return ResultUtils.success();
    }

    /**
     * 查询相似题 A/B 实验历史记录。
     */
    @ApiOperation("A/B实验历史")
    @PreAuthorize("hasAuthority('system:questionBank:questionExperimentHistory')")
    @GetMapping("/questionExperimentHistory")
    public Result<List<QuestionExperimentHistoryResp>> questionExperimentHistory() {
        return ResultUtils.success(questionExperimentService.questionExperimentHistory());
    }
}
