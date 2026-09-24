package com.study.module.system.questionbank.controller;

import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.questionbank.dto.request.QuestionVariantGenerateReq;
import com.study.module.system.questionbank.dto.request.QuestionVariantTemplateSaveReq;
import com.study.module.system.questionbank.dto.response.QuestionVariantGenerateResp;
import com.study.module.system.questionbank.dto.response.QuestionVariantTemplateResp;
import com.study.module.system.questionvariant.service.QuestionVariantService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 参数化变式题管理；生成结果统一进入精品题库待人工审核。 */
@Api(tags = "参数化变式题")
@RestController
@RequestMapping("/api/questionVariant")
public class QuestionVariantController {
    @Autowired private QuestionVariantService questionVariantService;

    @ApiOperation("变式题模板列表")
    @PreAuthorize("hasAuthority('system:questionBank:questionVariantTemplateList')")
    @GetMapping("/questionVariantTemplateList")
    public Result<List<QuestionVariantTemplateResp>> questionVariantTemplateList(@RequestParam(required = false) String subject) {
        return ResultUtils.success(questionVariantService.questionVariantTemplateList(subject));
    }

    @ApiOperation("保存变式题模板")
    @PreAuthorize("hasAuthority('system:questionBank:saveQuestionVariantTemplate')")
    @PostMapping("/saveQuestionVariantTemplate")
    public Result<Void> saveQuestionVariantTemplate(@RequestBody @Validated QuestionVariantTemplateSaveReq request) {
        questionVariantService.saveQuestionVariantTemplate(request);
        return ResultUtils.success();
    }

    @ApiOperation("生成并校验变式题")
    @PreAuthorize("hasAuthority('system:questionBank:generateQuestionVariant')")
    @PostMapping("/generateQuestionVariant")
    public Result<QuestionVariantGenerateResp> generateQuestionVariant(@RequestBody @Validated QuestionVariantGenerateReq request) {
        return ResultUtils.success(questionVariantService.generateQuestionVariant(request));
    }
}
