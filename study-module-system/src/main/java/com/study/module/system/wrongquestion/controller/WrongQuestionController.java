package com.study.module.system.wrongquestion.controller;

import com.study.module.system.wrongquestion.dto.request.CreateWrongQuestionReq;
import com.study.module.system.wrongquestion.dto.request.ImportWrongQuestionImageReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureTaskCreateReq;
import com.study.module.system.wrongquestion.dto.request.UpdateWrongQuestionImageReq;
import com.study.module.system.wrongquestion.dto.request.UpdateWrongQuestionReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionBatchDeleteReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionCorrectionRecordReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionErrorAnalysisReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionErrorAnalysisStatisticsReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionIdReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionKnowledgePointBindReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionKnowledgePointStatisticsReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionPageListReq;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionStatusUpdateReq;
import com.study.module.system.wrongquestion.dto.response.ImportWrongQuestionImageResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionCorrectionRecordResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionDetailResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionErrorAnalysisStatisticsResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionKnowledgePointStatisticsResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionPageListResp;
import com.study.module.system.wrongquestion.service.WrongQuestionCorrectionRecordService;
import com.study.module.system.wrongquestion.service.WrongQuestionCreateService;
import com.study.module.system.wrongquestion.service.WrongQuestionDeleteService;
import com.study.module.system.wrongquestion.service.WrongQuestionDetailService;
import com.study.module.system.wrongquestion.service.WrongQuestionErrorAnalysisService;
import com.study.module.system.wrongquestion.service.WrongQuestionImportService;
import com.study.module.system.wrongquestion.service.QuestionCaptureService;
import com.study.module.system.wrongquestion.service.WrongQuestionKnowledgePointBindService;
import com.study.module.system.wrongquestion.service.WrongQuestionListService;
import com.study.module.system.wrongquestion.service.WrongQuestionStatusService;
import com.study.module.system.wrongquestion.service.WrongQuestionUpdateService;
import com.study.common.core.domain.Result;
import com.study.common.core.domain.dto.PageResult;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.common.core.utils.ResultUtils;
import com.yunshang.budget.common.security.utils.AccountUtils;
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
import java.util.Collections;

/**
 * 初中生错题归档前端控制器
 */
@Api(tags = "初中生错题归档")
@RestController
@RequestMapping("/api/wrongQuestion")
public class WrongQuestionController {

    @Autowired
    WrongQuestionListService wrongQuestionListService;

    @Autowired
    WrongQuestionDetailService wrongQuestionDetailService;

    @Autowired
    WrongQuestionCreateService wrongQuestionCreateService;

    @Autowired
    WrongQuestionUpdateService wrongQuestionUpdateService;

    @Autowired
    WrongQuestionDeleteService wrongQuestionDeleteService;

    @Autowired
    WrongQuestionImportService wrongQuestionImportService;

    @Autowired
    QuestionCaptureService questionCaptureService;

    @Autowired
    WrongQuestionStatusService wrongQuestionStatusService;

    @Autowired
    WrongQuestionCorrectionRecordService wrongQuestionCorrectionRecordService;

    @Autowired
    WrongQuestionKnowledgePointBindService wrongQuestionKnowledgePointBindService;

    @Autowired
    WrongQuestionErrorAnalysisService wrongQuestionErrorAnalysisService;

    /**
     * 错题分页列表
     */
    @ApiOperation("错题分页列表")
    @PreAuthorize("hasAuthority('system:wrongQuestion:wrongQuestionPageList')")
    @GetMapping("/wrongQuestionPageList")
    public Result<PageResult<WrongQuestionPageListResp>> wrongQuestionPageList(@Validated WrongQuestionPageListReq request) {
        return ResultUtils.success(wrongQuestionListService.wrongQuestionPageList(request));
    }

    /**
     * 错题详情
     */
    @ApiOperation("错题详情")
    @PreAuthorize("hasAuthority('system:wrongQuestion:wrongQuestionDetail')")
    @GetMapping("/wrongQuestionDetail")
    public Result<WrongQuestionDetailResp> wrongQuestionDetail(@Validated WrongQuestionIdReq request) {
        return ResultUtils.success(wrongQuestionDetailService.wrongQuestionDetail(request.getId()));
    }

    /**
     * 错题录入
     */
    @ApiOperation("错题录入")
    @PreAuthorize("hasAuthority('system:wrongQuestion:createWrongQuestion')")
    @PostMapping("/createWrongQuestion")
    public Result<Void> createWrongQuestion(@RequestBody @Validated CreateWrongQuestionReq request) {
        wrongQuestionCreateService.createWrongQuestion(request);
        return ResultUtils.success();
    }

    /**
     * A4图片识别导入错题
     */
    @ApiOperation("A4图片识别导入错题")
    @PreAuthorize("hasAuthority('system:wrongQuestion:importWrongQuestionImage')")
    @PostMapping("/importWrongQuestionImage")
    public Result<ImportWrongQuestionImageResp> importWrongQuestionImage(@RequestBody @Validated ImportWrongQuestionImageReq request) {
        QuestionCaptureTaskCreateReq captureRequest = new QuestionCaptureTaskCreateReq();
        captureRequest.setGrade(request.getGrade());
        captureRequest.setSubject(request.getSubject());
        captureRequest.setQuestionType(request.getQuestionType() == null || request.getQuestionType().trim().isEmpty() ? "其他" : request.getQuestionType());
        captureRequest.setSource(request.getSource());
        captureRequest.setLearningPoint(request.getLearningPoint());
        captureRequest.setErrorLabels(request.getErrorLabels());
        captureRequest.setImageFileIds(Collections.singletonList(request.getImageFileId()));
        ImportWrongQuestionImageResp response = new ImportWrongQuestionImageResp();
        response.setImportCount(0);
        response.setTaskId(questionCaptureService.createQuestionCaptureTaskByUserId(captureRequest, AccountUtils.getUserId()));
        return ResultUtils.success(response);
    }

    /**
     * 错题修改
     */
    @ApiOperation("错题修改")
    @PreAuthorize("hasAuthority('system:wrongQuestion:updateWrongQuestion')")
    @PostMapping("/updateWrongQuestion")
    public Result<Void> updateWrongQuestion(@RequestBody @Validated UpdateWrongQuestionReq request) {
        wrongQuestionUpdateService.updateWrongQuestion(request);
        return ResultUtils.success();
    }

    /**
     * 错图修改
     */
    @ApiOperation("错图修改")
    @PreAuthorize("hasAuthority('system:wrongQuestion:updateWrongQuestionImage')")
    @PostMapping("/updateWrongQuestionImage")
    public Result<Void> updateWrongQuestionImage(@RequestBody @Validated UpdateWrongQuestionImageReq request) {
        wrongQuestionUpdateService.updateWrongQuestionImage(request);
        return ResultUtils.success();
    }

    /**
     * 错题状态流转
     */
    @ApiOperation("错题状态流转")
    @PreAuthorize("hasAuthority('system:wrongQuestion:updateWrongQuestionStatus')")
    @PostMapping("/updateWrongQuestionStatus")
    public Result<Void> updateWrongQuestionStatus(@RequestBody @Validated WrongQuestionStatusUpdateReq request) {
        wrongQuestionStatusService.updateWrongQuestionStatus(request.getId(), request.getStatus(), request.getRemark());
        return ResultUtils.success();
    }

    /**
     * 提交错题订正记录
     */
    @ApiOperation("提交错题订正记录")
    @PreAuthorize("hasAuthority('system:wrongQuestion:submitCorrectionRecord')")
    @PostMapping("/submitCorrectionRecord")
    public Result<Void> submitCorrectionRecord(@RequestBody @Validated WrongQuestionCorrectionRecordReq request) {
        wrongQuestionCorrectionRecordService.submitCorrectionRecord(request);
        return ResultUtils.success();
    }

    /**
     * 错题订正记录列表
     */
    @ApiOperation("错题订正记录列表")
    @PreAuthorize("hasAuthority('system:wrongQuestion:correctionRecordList')")
    @GetMapping("/correctionRecordList")
    public Result<List<WrongQuestionCorrectionRecordResp>> correctionRecordList(@Validated WrongQuestionIdReq request) {
        return ResultUtils.success(wrongQuestionCorrectionRecordService.correctionRecordList(request.getId()));
    }

    /**
     * 绑定错题知识点
     */
    @ApiOperation("绑定错题知识点")
    @PreAuthorize("hasAuthority('system:wrongQuestion:bindWrongQuestionKnowledgePoint')")
    @PostMapping("/bindWrongQuestionKnowledgePoint")
    public Result<Void> bindWrongQuestionKnowledgePoint(@RequestBody @Validated WrongQuestionKnowledgePointBindReq request) {
        wrongQuestionKnowledgePointBindService.bindWrongQuestionKnowledgePoint(request);
        return ResultUtils.success();
    }

    /**
     * 错题知识点统计
     */
    @ApiOperation("错题知识点统计")
    @PreAuthorize("hasAuthority('system:wrongQuestion:wrongQuestionKnowledgePointStatistics')")
    @GetMapping("/wrongQuestionKnowledgePointStatistics")
    public Result<List<WrongQuestionKnowledgePointStatisticsResp>> wrongQuestionKnowledgePointStatistics(
            @Validated WrongQuestionKnowledgePointStatisticsReq request) {
        return ResultUtils.success(wrongQuestionKnowledgePointBindService.wrongQuestionKnowledgePointStatistics(request));
    }

    /**
     * 更新错题错因分析
     */
    @ApiOperation("更新错题错因分析")
    @PreAuthorize("hasAuthority('system:wrongQuestion:updateWrongQuestionErrorAnalysis')")
    @PostMapping("/updateWrongQuestionErrorAnalysis")
    public Result<Void> updateWrongQuestionErrorAnalysis(@RequestBody @Validated WrongQuestionErrorAnalysisReq request) {
        wrongQuestionErrorAnalysisService.updateWrongQuestionErrorAnalysis(request);
        return ResultUtils.success();
    }

    /**
     * 错因分析统计
     */
    @ApiOperation("错因分析统计")
    @PreAuthorize("hasAuthority('system:wrongQuestion:wrongQuestionErrorAnalysisStatistics')")
    @GetMapping("/wrongQuestionErrorAnalysisStatistics")
    public Result<List<WrongQuestionErrorAnalysisStatisticsResp>> wrongQuestionErrorAnalysisStatistics(
            @Validated WrongQuestionErrorAnalysisStatisticsReq request) {
        return ResultUtils.success(wrongQuestionErrorAnalysisService.wrongQuestionErrorAnalysisStatistics(request));
    }

    /**
     * 错题删除
     */
    @ApiOperation("错题删除")
    @PreAuthorize("hasAuthority('system:wrongQuestion:deleteWrongQuestion')")
    @PostMapping("/deleteWrongQuestion")
    public Result<Void> deleteWrongQuestion(@RequestBody @Validated WrongQuestionIdReq request) {
        wrongQuestionDeleteService.deleteWrongQuestion(request.getId());
        return ResultUtils.success();
    }

    /**
     * 错题批量删除
     */
    @ApiOperation("错题批量删除")
    @PreAuthorize("hasAuthority('system:wrongQuestion:batchDeleteWrongQuestion')")
    @PostMapping("/batchDeleteWrongQuestion")
    public Result<Void> batchDeleteWrongQuestion(@RequestBody @Validated WrongQuestionBatchDeleteReq request) {
        wrongQuestionDeleteService.batchDeleteWrongQuestion(request.getIds());
        return ResultUtils.success();
    }
}
