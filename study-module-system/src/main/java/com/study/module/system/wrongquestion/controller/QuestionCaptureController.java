package com.study.module.system.wrongquestion.controller;

import com.study.common.core.domain.Result;
import com.study.common.core.domain.dto.PageResult;
import com.study.common.core.utils.ResultUtils;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureConfirmReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureDuplicateCheckReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCapturePageIdReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionIdsReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionIdReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionCreateReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionSplitReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionSnapshotReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionUpdateReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureTaskCreateReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureTaskIdReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureTaskPageListReq;
import com.study.module.system.wrongquestion.dto.response.QuestionCaptureTaskResp;
import com.study.module.system.wrongquestion.dto.response.QuestionCaptureDuplicateResp;
import com.study.module.system.wrongquestion.dto.response.QuestionCaptureTaskPageListResp;
import com.study.module.system.wrongquestion.service.QuestionCaptureService;
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
 * 题目采集控制器。
 */
@Api(tags = "题目采集中心")
@RestController
@RequestMapping("/api/questionCapture")
public class QuestionCaptureController {

    @Autowired
    private QuestionCaptureService questionCaptureService;

    /**
     * 创建后立即返回任务ID，OCR、PDF分页等耗时操作在后台执行。
     */
    @ApiOperation("创建题目采集任务")
    @PreAuthorize("hasAuthority('system:wrongQuestion:createQuestionCaptureTask')")
    @PostMapping("/createQuestionCaptureTask")
    public Result<Long> createQuestionCaptureTask(@RequestBody @Validated QuestionCaptureTaskCreateReq request) {
        return ResultUtils.success(questionCaptureService.createQuestionCaptureTask(request));
    }

    /**
     * 仅返回当前登录学生自己的历史采集任务，供继续确认和失败重试。
     */
    @ApiOperation("题目采集任务分页列表")
    @PreAuthorize("hasAuthority('system:wrongQuestion:questionCaptureTaskPageList')")
    @GetMapping("/questionCaptureTaskPageList")
    public Result<PageResult<QuestionCaptureTaskPageListResp>> questionCaptureTaskPageList(
            @Validated QuestionCaptureTaskPageListReq request) {
        return ResultUtils.success(questionCaptureService.questionCaptureTaskPageList(request));
    }

    /**
     * 返回任务页面、OCR题块及原图/净化图文件ID。
     */
    @ApiOperation("题目采集任务详情")
    @PreAuthorize("hasAuthority('system:wrongQuestion:questionCaptureTaskDetail')")
    @GetMapping("/questionCaptureTaskDetail")
    public Result<QuestionCaptureTaskResp> questionCaptureTaskDetail(@Validated QuestionCaptureTaskIdReq request) {
        return ResultUtils.success(questionCaptureService.questionCaptureTaskDetail(request.getId()));
    }

    /**
     * 重试整个题目采集任务。
     */
    @ApiOperation("重试题目采集任务")
    @PreAuthorize("hasAuthority('system:wrongQuestion:retryQuestionCaptureTask')")
    @PostMapping("/retryQuestionCaptureTask")
    public Result<Void> retryQuestionCaptureTask(@RequestBody @Validated QuestionCaptureTaskIdReq request) {
        questionCaptureService.retryQuestionCaptureTask(request.getId());
        return ResultUtils.success();
    }

    /**
     * 保存待确认题块的人工修正内容。
     */
    @ApiOperation("修改待确认题块")
    @PreAuthorize("hasAuthority('system:wrongQuestion:updateQuestionCaptureRegion')")
    @PostMapping("/updateQuestionCaptureRegion")
    public Result<Void> updateQuestionCaptureRegion(@RequestBody @Validated QuestionCaptureRegionUpdateReq request) {
        questionCaptureService.updateQuestionCaptureRegion(request);
        return ResultUtils.success();
    }

    /**
     * 人工新增 OCR 漏掉的题块。
     */
    @ApiOperation("新增待确认题块")
    @PreAuthorize("hasAuthority('system:wrongQuestion:createQuestionCaptureRegion')")
    @PostMapping("/createQuestionCaptureRegion")
    public Result<Void> createQuestionCaptureRegion(@RequestBody @Validated QuestionCaptureRegionCreateReq request) {
        questionCaptureService.createQuestionCaptureRegion(request);
        return ResultUtils.success();
    }

    /**
     * 合并同一页面的待确认题块。
     */
    @ApiOperation("合并题块")
    @PreAuthorize("hasAuthority('system:wrongQuestion:mergeQuestionCaptureRegion')")
    @PostMapping("/mergeQuestionCaptureRegion")
    public Result<Void> mergeQuestionCaptureRegion(@RequestBody @Validated QuestionCaptureRegionIdsReq request) {
        questionCaptureService.mergeQuestionCaptureRegion(request);
        return ResultUtils.success();
    }

    /**
     * 将待确认题块拆分为两块。
     */
    @ApiOperation("拆分题块")
    @PreAuthorize("hasAuthority('system:wrongQuestion:splitQuestionCaptureRegion')")
    @PostMapping("/splitQuestionCaptureRegion")
    public Result<Void> splitQuestionCaptureRegion(@RequestBody @Validated QuestionCaptureRegionIdReq request) {
        questionCaptureService.splitQuestionCaptureRegion(request.getId());
        return ResultUtils.success();
    }

    /**
     * 按用户给定比例切分题块，保留旧接口以兼容固定二等分调用。
     */
    @ApiOperation("按比例拆分题块")
    @PreAuthorize("hasAuthority('system:wrongQuestion:splitQuestionCaptureRegion')")
    @PostMapping("/splitQuestionCaptureRegionByRatio")
    public Result<Void> splitQuestionCaptureRegionByRatio(@RequestBody @Validated QuestionCaptureRegionSplitReq request) {
        questionCaptureService.splitQuestionCaptureRegion(request);
        return ResultUtils.success();
    }

    @ApiOperation("恢复题目采集题块快照")
    @PreAuthorize("hasAuthority('system:wrongQuestion:restoreQuestionCaptureRegionSnapshot')")
    @PostMapping("/restoreQuestionCaptureRegionSnapshot")
    public Result<Void> restoreQuestionCaptureRegionSnapshot(@RequestBody @Validated QuestionCaptureRegionSnapshotReq request) {
        questionCaptureService.restoreQuestionCaptureRegionSnapshot(request);
        return ResultUtils.success();
    }

    /**
     * 跳过待确认题块。
     */
    @ApiOperation("删除题块")
    @PreAuthorize("hasAuthority('system:wrongQuestion:deleteQuestionCaptureRegion')")
    @PostMapping("/deleteQuestionCaptureRegion")
    public Result<Void> deleteQuestionCaptureRegion(@RequestBody @Validated QuestionCaptureRegionIdReq request) {
        questionCaptureService.deleteQuestionCaptureRegion(request.getId());
        return ResultUtils.success();
    }

    /**
     * 恢复已跳过的题块。
     */
    @ApiOperation("恢复已跳过题块")
    @PreAuthorize("hasAuthority('system:wrongQuestion:restoreQuestionCaptureRegion')")
    @PostMapping("/restoreQuestionCaptureRegion")
    public Result<Void> restoreQuestionCaptureRegion(@RequestBody @Validated QuestionCaptureRegionIdReq request) {
        questionCaptureService.restoreQuestionCaptureRegion(request.getId());
        return ResultUtils.success();
    }

    /**
     * 重试单页 OCR 识别。
     */
    @ApiOperation("重试采集页面OCR")
    @PreAuthorize("hasAuthority('system:wrongQuestion:retryQuestionCapturePage')")
    @PostMapping("/retryQuestionCapturePage")
    public Result<Void> retryQuestionCapturePage(@RequestBody @Validated QuestionCaptureRegionIdReq request) {
        questionCaptureService.retryQuestionCapturePage(request.getId());
        return ResultUtils.success();
    }

    /**
     * OCR 或 PDF 分页失败时，仍允许以原图建立待确认错题。
     */
    @ApiOperation("仅保存采集页面图片")
    @PreAuthorize("hasAuthority('system:wrongQuestion:saveQuestionCapturePageAsImage')")
    @PostMapping("/saveQuestionCapturePageAsImage")
    public Result<Void> saveQuestionCapturePageAsImage(@RequestBody @Validated QuestionCapturePageIdReq request) {
        questionCaptureService.saveQuestionCapturePageAsImage(request.getId());
        return ResultUtils.success();
    }

    /**
     * 在创建错题前提示当前学生已经收录的完全重复题目。
     */
    @ApiOperation("采集题块重复错题检查")
    @PreAuthorize("hasAuthority('system:wrongQuestion:questionCaptureDuplicateList')")
    @PostMapping("/questionCaptureDuplicateList")
    public Result<List<QuestionCaptureDuplicateResp>> questionCaptureDuplicateList(
            @RequestBody @Validated QuestionCaptureDuplicateCheckReq request) {
        return ResultUtils.success(questionCaptureService.questionCaptureDuplicateList(request));
    }

    /**
     * 确认题块并创建错题。
     */
    @ApiOperation("确认采集题块并创建错题")
    @PreAuthorize("hasAuthority('system:wrongQuestion:confirmQuestionCapture')")
    @PostMapping("/confirmQuestionCapture")
    public Result<Integer> confirmQuestionCapture(@RequestBody @Validated QuestionCaptureConfirmReq request) {
        return ResultUtils.success(questionCaptureService.confirmQuestionCapture(request));
    }
}
