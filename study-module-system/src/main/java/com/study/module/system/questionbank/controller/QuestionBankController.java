package com.study.module.system.questionbank.controller;

import com.study.common.core.domain.Result;
import com.study.common.core.domain.dto.PageResult;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.questionbank.dto.request.*;
import com.study.module.system.questionbank.dto.response.QuestionBankDuplicatePairResp;
import com.study.module.system.questionbank.dto.response.QuestionBankDuplicateResp;
import com.study.module.system.questionbank.dto.response.QuestionBankDetailResp;
import com.study.module.system.questionbank.dto.response.QuestionBankPageListResp;
import com.study.module.system.questionbank.dto.response.QuestionBankVersionListResp;
import com.study.module.system.questionbank.dto.response.QuestionBankReviewHistoryResp;
import com.study.module.system.questionbank.dto.response.QuestionBankImportResp;
import com.study.module.system.questionbank.service.QuestionBankDeleteService;
import com.study.module.system.questionbank.service.QuestionBankDuplicateService;
import com.study.module.system.questionbank.service.QuestionBankDetailService;
import com.study.module.system.questionbank.service.QuestionBankListService;
import com.study.module.system.questionbank.service.QuestionBankReviewService;
import com.study.module.system.questionbank.service.QuestionBankSaveService;
import com.study.module.system.questionbank.service.QuestionBankHistoryService;
import com.study.module.system.questionbank.service.QuestionBankImportService;
import com.study.module.system.questionbank.service.QuestionBankBatchReviewService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 题库题目接口
 */
@Api(tags = "精品题库")
@RestController
@RequestMapping("/api/questionBank")
public class QuestionBankController {

    @Autowired
    QuestionBankListService questionBankListService;

    @Autowired
    QuestionBankDetailService questionBankDetailService;

    @Autowired
    QuestionBankSaveService questionBankSaveService;

    @Autowired
    QuestionBankDeleteService questionBankDeleteService;

    @Autowired
    QuestionBankReviewService questionBankReviewService;

    @Autowired
    QuestionBankDuplicateService questionBankDuplicateService;

    @Autowired
    QuestionBankHistoryService questionBankHistoryService;

    @Autowired
    QuestionBankImportService questionBankImportService;

    @Autowired
    QuestionBankBatchReviewService questionBankBatchReviewService;

    @ApiOperation("题库分页列表")
    @PreAuthorize("hasAuthority('system:questionBank:questionBankPageList')")
    @GetMapping("/questionBankPageList")
    public Result<PageResult<QuestionBankPageListResp>> questionBankPageList(
            @Validated QuestionBankPageListReq request) {
        return ResultUtils.success(questionBankListService.questionBankPageList(request));
    }

    @ApiOperation("题库详情")
    @PreAuthorize("hasAuthority('system:questionBank:questionBankDetail')")
    /**
     * 查询题库详情。
     */
    @GetMapping("/questionBankDetail")
    public Result<QuestionBankDetailResp> questionBankDetail(@Validated QuestionBankIdReq request) {
        return ResultUtils.success(questionBankDetailService.questionBankDetail(request.getId()));
    }

    @ApiOperation("保存题库题目")
    @PreAuthorize("hasAuthority('system:questionBank:saveQuestionBank')")
    /**
     * 创建或保存题库。
     */
    @PostMapping("/saveQuestionBank")
    public Result<Void> saveQuestionBank(@RequestBody @Validated QuestionBankSaveReq request) {
        questionBankSaveService.saveQuestionBank(request);
        return ResultUtils.success();
    }

    @ApiOperation("删除题库题目")
    @PreAuthorize("hasAuthority('system:questionBank:deleteQuestionBank')")
    /**
     * 删除题库。
     */
    @PostMapping("/deleteQuestionBank")
    public Result<Void> deleteQuestionBank(@RequestBody @Validated QuestionBankIdReq request) {
        questionBankDeleteService.deleteQuestionBank(request.getId());
        return ResultUtils.success();
    }

    @ApiOperation("审核题库题目")
    @PreAuthorize("hasAuthority('system:questionBank:reviewQuestionBank')")
    /**
     * 执行 reviewQuestionBank 业务处理。
     */
    @PostMapping("/reviewQuestionBank")
    public Result<Void> reviewQuestionBank(@RequestBody @Validated QuestionBankReviewReq request) {
        questionBankReviewService.reviewQuestionBank(request);
        return ResultUtils.success();
    }

    @ApiOperation("保存前重复题检测")
    @PreAuthorize("hasAuthority('system:questionBank:duplicateQuestionList')")
    @PostMapping("/duplicateQuestionList")
    public Result<List<QuestionBankDuplicateResp>> duplicateQuestionList(
            @RequestBody @Validated QuestionBankDuplicateCheckReq request) {
        return ResultUtils.success(questionBankDuplicateService.duplicateQuestionList(request));
    }

    @ApiOperation("历史重复题扫描")
    @PreAuthorize("hasAuthority('system:questionBank:duplicateQuestionHistory')")
    @GetMapping("/duplicateQuestionHistory")
    public Result<List<QuestionBankDuplicatePairResp>> duplicateQuestionHistory(
            @Validated QuestionBankDuplicateHistoryReq request) {
        return ResultUtils.success(questionBankDuplicateService.duplicateQuestionHistory(request));
    }

    @ApiOperation("清理历史重复题")
    @PreAuthorize("hasAuthority('system:questionBank:cleanDuplicateQuestion')")
    /**
     * 清理相关业务数据。
     */
    @PostMapping("/cleanDuplicateQuestion")
    public Result<Void> cleanDuplicateQuestion(@RequestBody @Validated QuestionBankDuplicateCleanReq request) {
        questionBankDuplicateService.cleanDuplicateQuestion(request);
        return ResultUtils.success();
    }

    @ApiOperation("人工合并高相似题")
    @PreAuthorize("hasAuthority('system:questionBank:mergeDuplicateQuestion')")
    /**
     * 合并相关业务数据。
     */
    @PostMapping("/mergeDuplicateQuestion")
    public Result<Void> mergeDuplicateQuestion(@RequestBody @Validated QuestionBankDuplicateCleanReq request) {
        questionBankDuplicateService.mergeDuplicateQuestion(request);
        return ResultUtils.success();
    }

    @ApiOperation("题目版本列表")
    @PreAuthorize("hasAuthority('system:questionBank:questionBankVersionList')")
    /**
     * 执行 questionBankVersionList 业务处理。
     */
    @GetMapping("/questionBankVersionList")
    public Result<List<QuestionBankVersionListResp>> questionBankVersionList(@Validated QuestionBankIdReq request) {
        return ResultUtils.success(questionBankHistoryService.questionBankVersionList(request.getId()));
    }

    @ApiOperation("题目审核历史")
    @PreAuthorize("hasAuthority('system:questionBank:questionBankReviewHistory')")
    /**
     * 查询题库历史记录。
     */
    @GetMapping("/questionBankReviewHistory")
    public Result<List<QuestionBankReviewHistoryResp>> questionBankReviewHistory(@Validated QuestionBankIdReq request) {
        return ResultUtils.success(questionBankHistoryService.questionBankReviewHistory(request.getId()));
    }

    @ApiOperation("A4文件识别导入题库")
    @PreAuthorize("hasAuthority('system:questionBank:importQuestionBankFile')")
    @PostMapping("/importQuestionBankFile")
    public Result<QuestionBankImportResp> importQuestionBankFile(
            @RequestBody @Validated QuestionBankImportReq request) {
        return ResultUtils.success(questionBankImportService.importQuestionBankFile(request));
    }

    @ApiOperation("批量审核题库题目")
    @PreAuthorize("hasAuthority('system:questionBank:batchReviewQuestionBank')")
    /**
     * 执行 batchReviewQuestionBank 业务处理。
     */
    @PostMapping("/batchReviewQuestionBank")
    public Result<Void> batchReviewQuestionBank(@RequestBody @Validated QuestionBankBatchReviewReq request) {
        questionBankBatchReviewService.batchReviewQuestionBank(request);
        return ResultUtils.success();
    }

}
