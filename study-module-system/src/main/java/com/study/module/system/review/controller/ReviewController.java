package com.study.module.system.review.controller;

import com.study.module.system.review.dto.request.PracticeAnswerSubmitReq;
import com.study.module.system.review.dto.request.PracticeAnswerBatchSubmitReq;
import com.study.module.system.review.dto.request.PracticeAnswerDraftSaveReq;
import com.study.module.system.review.dto.request.PracticePaperAnswerFillReq;
import com.study.module.system.review.dto.request.PracticePaperFillDetailReq;
import com.study.module.system.review.dto.request.PracticeSessionCreateReq;
import com.study.module.system.review.dto.request.PracticeSessionIdReq;
import com.study.module.system.review.dto.request.PracticeSessionQuestionIdReq;
import com.study.module.system.review.dto.request.PracticeSessionPageListReq;
import com.study.module.system.review.dto.request.ReviewItemIdReq;
import com.study.module.system.review.dto.request.ReviewAnswerDraftSaveReq;
import com.study.module.system.review.dto.request.ReviewHistoryPageListReq;
import com.study.module.system.review.dto.request.ReviewReminderIdReq;
import com.study.module.system.review.dto.request.ReviewReminderPageListReq;
import com.study.module.system.review.dto.request.ReviewWeakPointPracticeReq;
import com.study.module.system.review.dto.request.ReviewTypicalPracticeReq;
import com.study.module.system.review.dto.request.ReviewLearningReportReq;
import com.study.module.system.review.dto.request.SubmitReviewFeedbackReq;
import com.study.module.system.review.dto.request.UpdateReviewPlanSettingReq;
import com.study.module.system.review.dto.request.UpdateLearningProfileReq;
import com.study.module.system.review.dto.request.ReviewExamSprintSaveReq;
import com.study.module.system.review.dto.response.PracticeAnswerSubmitResp;
import com.study.module.system.review.dto.response.PracticeQuestionAnswerResp;
import com.study.module.system.review.dto.response.PracticePaperFillDetailResp;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import com.study.module.system.review.dto.response.PracticeSessionPreviewResp;
import com.study.module.system.review.dto.response.PracticeSessionPageListResp;
import com.study.module.system.review.dto.response.PracticeStatisticsResp;
import com.study.module.system.review.dto.response.ReviewAnswerResp;
import com.study.module.system.review.dto.response.ReviewHomeResp;
import com.study.module.system.review.dto.response.ReviewHistoryHomeResp;
import com.study.module.system.review.dto.response.ReviewPlanSettingResp;
import com.study.module.system.review.dto.response.ReviewReminderHomeResp;
import com.study.module.system.review.dto.response.SubmitReviewFeedbackResp;
import com.study.module.system.review.dto.response.ReviewTodayHomeResp;
import com.study.module.system.review.dto.response.ReviewWeakPointPracticeResp;
import com.study.module.system.review.dto.response.ReviewTypicalPracticeResp;
import com.study.module.system.review.dto.response.ReviewAdaptiveContentResp;
import com.study.module.system.review.dto.response.ReviewLearningReportResp;
import com.study.module.system.review.dto.response.LearningDataBackupResp;
import com.study.module.system.review.dto.response.LearningProfileResp;
import com.study.module.system.review.dto.response.ReviewExamSprintResp;
import com.study.module.system.review.dto.response.LearningPathResp;
import com.study.module.system.review.service.PracticeAnswerSubmitService;
import com.study.module.system.review.service.PracticeAnswerRevealService;
import com.study.module.system.review.service.PracticePaperFillService;
import com.study.module.system.review.service.PracticeSessionCreateService;
import com.study.module.system.review.service.PracticeSessionDetailService;
import com.study.module.system.review.service.PracticeSessionFinishService;
import com.study.module.system.review.service.PracticeSessionListService;
import com.study.module.system.review.service.PracticeStatisticsService;
import com.study.module.system.review.service.ReviewAnswerService;
import com.study.module.system.review.service.ReviewFeedbackService;
import com.study.module.system.review.service.ReviewHomeService;
import com.study.module.system.review.service.ReviewHistoryListService;
import com.study.module.system.review.service.ReviewPlanSettingService;
import com.study.module.system.review.service.ReviewReminderListService;
import com.study.module.system.review.service.ReviewReminderUpdateService;
import com.study.module.system.review.service.ReviewTodayService;
import com.study.module.system.review.service.ReviewWeakPointPracticeService;
import com.study.module.system.review.service.ReviewTypicalPracticeService;
import com.study.module.system.review.service.ReviewAdaptiveContentService;
import com.study.module.system.review.service.ReviewLearningReportService;
import com.study.module.system.review.service.LearningDataBackupService;
import com.study.module.system.review.service.LearningProfileService;
import com.study.module.system.review.service.PracticePaperExportTaskService;
import com.study.module.system.review.service.ReviewExamSprintPlanService;
import com.study.module.system.review.service.LearningPathService;
import com.study.module.system.review.dto.request.PracticePaperExportCreateReq;
import com.study.module.system.review.dto.response.PracticePaperExportTaskResp;
import com.study.common.core.domain.Result;
import com.study.common.core.domain.dto.PageResult;
import com.study.common.core.utils.ResultUtils;
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

/**
 * 智能复习前端控制器
 */
@Api(tags = "智能复习")
@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    ReviewHomeService reviewHomeService;

    @Autowired
    ReviewTodayService reviewTodayService;

    @Autowired
    ReviewAnswerService reviewAnswerService;

    @Autowired
    ReviewFeedbackService reviewFeedbackService;

    @Autowired
    ReviewPlanSettingService reviewPlanSettingService;

    @Autowired
    ReviewHistoryListService reviewHistoryListService;

    @Autowired
    ReviewReminderListService reviewReminderListService;

    @Autowired
    ReviewReminderUpdateService reviewReminderUpdateService;

    @Autowired
    ReviewWeakPointPracticeService reviewWeakPointPracticeService;

    @Autowired
    ReviewTypicalPracticeService reviewTypicalPracticeService;

    @Autowired
    ReviewAdaptiveContentService reviewAdaptiveContentService;

    @Autowired
    ReviewLearningReportService reviewLearningReportService;

    @Autowired
    PracticeSessionCreateService practiceSessionCreateService;

    @Autowired
    PracticeSessionDetailService practiceSessionDetailService;

    @Autowired
    PracticeAnswerSubmitService practiceAnswerSubmitService;

    @Autowired
    PracticeAnswerRevealService practiceAnswerRevealService;

    @Autowired
    PracticeSessionFinishService practiceSessionFinishService;

    @Autowired
    PracticeSessionListService practiceSessionListService;

    @Autowired
    PracticeStatisticsService practiceStatisticsService;

    @Autowired
    LearningProfileService learningProfileService;

    @Autowired
    LearningDataBackupService learningDataBackupService;

    @Autowired
    PracticePaperExportTaskService practicePaperExportTaskService;

    @Autowired
    LearningPathService learningPathService;

    @Autowired
    PracticePaperFillService practicePaperFillService;

    @Autowired
    ReviewExamSprintPlanService reviewExamSprintPlanService;

    /**
     * 初始化智能复习首页
     */
    @ApiOperation("初始化智能复习首页")
    @PreAuthorize("hasAuthority('system:review:initializeReviewHome')")
    @PostMapping("/initializeReviewHome")
    public Result<ReviewHomeResp> initializeReviewHome(
            @RequestParam(required = false) String subject) {
        return ResultUtils.success(reviewHomeService.initializeReviewHome(subject));
    }

    /**
     * 今日复习首页
     */
    @ApiOperation("今日复习首页")
    @PreAuthorize("hasAuthority('system:review:todayReviewHome')")
    @GetMapping("/todayReviewHome")
    public Result<ReviewTodayHomeResp> todayReviewHome(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) Integer taskLimit) {
        return ResultUtils.success(reviewTodayService.todayReviewHome(subject, taskLimit));
    }

    /**
     * 查看复习答案
     */
    @ApiOperation("查看复习答案")
    @PreAuthorize("hasAuthority('system:review:reviewAnswer')")
    @PostMapping("/reviewAnswer")
    public Result<ReviewAnswerResp> reviewAnswer(@RequestBody @Validated ReviewItemIdReq request) {
        return ResultUtils.success(reviewAnswerService.reviewAnswer(request.getReviewItemId()));
    }

    @ApiOperation("保存复习主动回忆答案")
    @PreAuthorize("hasAuthority('system:review:reviewAnswer')")
    @PostMapping("/saveReviewAnswerDraft")
    public Result<Void> saveReviewAnswerDraft(@RequestBody @Validated ReviewAnswerDraftSaveReq request) {
        reviewAnswerService.saveReviewAnswerDraft(request.getReviewItemId(), request.getStudentAnswer());
        return ResultUtils.success();
    }

    /**
     * 提交四级反馈
     */
    @ApiOperation("提交四级反馈")
    @PreAuthorize("hasAuthority('system:review:submitReviewFeedback')")
    @PostMapping("/submitReviewFeedback")
    public Result<SubmitReviewFeedbackResp> submitReviewFeedback(
            @RequestBody @Validated SubmitReviewFeedbackReq request) {
        return ResultUtils.success(reviewFeedbackService.submitReviewFeedback(request));
    }

    /**
     * 根据薄弱知识点生成个人专项练习
     */
    @ApiOperation("生成薄弱知识点专项练习")
    @PreAuthorize("hasAuthority('system:review:generateWeakPointPractice')")
    @PostMapping("/generateWeakPointPractice")
    public Result<ReviewWeakPointPracticeResp> generateWeakPointPractice(
            @RequestBody @Validated ReviewWeakPointPracticeReq request) {
        return ResultUtils.success(
                reviewWeakPointPracticeService.generateWeakPointPractice(request));
    }

    /**
     * 生成兼顾代表性和知识点覆盖的典型错题练习
     */
    @ApiOperation("生成典型错题练习")
    @PreAuthorize("hasAuthority('system:review:generateTypicalPractice')")
    @PostMapping("/generateTypicalPractice")
    public Result<ReviewTypicalPracticeResp> generateTypicalPractice(
            @RequestBody @Validated ReviewTypicalPracticeReq request) {
        return ResultUtils.success(reviewTypicalPracticeService.generateTypicalPractice(request));
    }

    /**
     * 根据最近作答动态推荐下一步学习内容
     */
    @ApiOperation("自适应学习内容")
    @PreAuthorize("hasAuthority('system:review:adaptiveLearningContent')")
    @GetMapping("/adaptiveLearningContent")
    public Result<ReviewAdaptiveContentResp> adaptiveLearningContent(
            @RequestParam(required = false) String subject) {
        return ResultUtils.success(reviewAdaptiveContentService.adaptiveLearningContent(subject));
    }

    /**
     * 学情报告。
     */
    @ApiOperation("学情报告")
    @PreAuthorize("hasAuthority('system:review:learningReport')")
    @GetMapping("/learningReport")
    public Result<ReviewLearningReportResp> learningReport(@Validated ReviewLearningReportReq request) {
        return ResultUtils.success(reviewLearningReportService.reviewLearningReport(request));
    }

    /**
     * 创建专项练习会话
     */
    @ApiOperation("创建专项练习会话")
    @PreAuthorize("hasAuthority('system:review:createPracticeSession')")
    @PostMapping("/createPracticeSession")
    public Result<PracticeSessionDetailResp> createPracticeSession(
            @RequestBody @Validated PracticeSessionCreateReq request) {
        return ResultUtils.success(practiceSessionCreateService.createPracticeSession(request));
    }

    /**
     * 预览专项练习组卷结果。
     */
    @ApiOperation("预览专项练习组卷结果")
    @PreAuthorize("hasAuthority('system:review:previewPracticeSession')")
    @PostMapping("/previewPracticeSession")
    public Result<PracticeSessionPreviewResp> previewPracticeSession(
            @RequestBody @Validated PracticeSessionCreateReq request) {
        return ResultUtils.success(practiceSessionCreateService.previewPracticeSession(request));
    }

    /**
     * 专项练习详情
     */
    @ApiOperation("专项练习详情")
    @PreAuthorize("hasAuthority('system:review:practiceSessionDetail')")
    @GetMapping("/practiceSessionDetail")
    public Result<PracticeSessionDetailResp> practiceSessionDetail(
            @Validated PracticeSessionIdReq request) {
        return ResultUtils.success(
                practiceSessionDetailService.practiceSessionDetail(request.getSessionId()));
    }

    /**
     * 用于打印、保存 PDF 和 Word 导出的个人练习详情。
     */
    @ApiOperation("个人练习卷导出详情")
    @PreAuthorize("hasAuthority('system:review:practicePaperDetail')")
    @GetMapping("/practicePaperDetail")
    public Result<PracticeSessionDetailResp> practicePaperDetail(
            @Validated PracticeSessionIdReq request) {
        return ResultUtils.success(
                practiceSessionDetailService.practicePaperDetail(request.getSessionId()));
    }

    @ApiOperation("创建服务端练习卷导出任务")
    @PreAuthorize("hasAuthority('system:review:practicePaperDetail')")
    @PostMapping("/createPracticePaperExportTask")
    public Result<Long> createPracticePaperExportTask(@RequestBody @Validated PracticePaperExportCreateReq request) {
        return ResultUtils.success(practicePaperExportTaskService.createPracticePaperExportTask(request));
    }

    @ApiOperation("练习卷导出历史")
    @PreAuthorize("hasAuthority('system:review:practicePaperDetail')")
    @GetMapping("/practicePaperExportTaskList")
    public Result<List<PracticePaperExportTaskResp>> practicePaperExportTaskList() {
        return ResultUtils.success(practicePaperExportTaskService.practicePaperExportTaskList());
    }

    /**
     * 通过纸面短码查询练习卷及其逐题回填记录。
     */
    @ApiOperation("纸面练习卷逐题回填详情")
    @PreAuthorize("hasAuthority('system:review:practiceSessionDetail')")
    @GetMapping("/practicePaperFillDetail")
    public Result<PracticePaperFillDetailResp> practicePaperFillDetail(
            @Validated PracticePaperFillDetailReq request) {
        return ResultUtils.success(practicePaperFillService.practicePaperFillDetail(request.getPaperCode()));
    }

    /**
     * 保存纸面练习卷逐题作答结果。
     */
    @ApiOperation("提交纸面练习卷逐题回填")
    @PreAuthorize("hasAuthority('system:review:submitPracticeAnswer')")
    @PostMapping("/submitPracticePaperAnswerFill")
    public Result<PracticePaperFillDetailResp> submitPracticePaperAnswerFill(
            @RequestBody @Validated PracticePaperAnswerFillReq request) {
        return ResultUtils.success(practicePaperFillService.submitPracticePaperAnswerFill(request));
    }

    /**
     * 提交专项练习作答
     */
    @ApiOperation("提交专项练习作答")
    @PreAuthorize("hasAuthority('system:review:submitPracticeAnswer')")
    @PostMapping("/submitPracticeAnswer")
    public Result<PracticeAnswerSubmitResp> submitPracticeAnswer(
            @RequestBody @Validated PracticeAnswerSubmitReq request) {
        return ResultUtils.success(practiceAnswerSubmitService.submitPracticeAnswer(request));
    }

    /**
     * 查看专项练习参考答案。
     */
    @ApiOperation("查看专项练习参考答案")
    @PreAuthorize("hasAuthority('system:review:practiceQuestionAnswer')")
    @GetMapping("/practiceQuestionAnswer")
    public Result<PracticeQuestionAnswerResp> practiceQuestionAnswer(
            @Validated PracticeSessionQuestionIdReq request) {
        return ResultUtils.success(
                practiceAnswerRevealService.practiceQuestionAnswer(request.getSessionQuestionId()));
    }

    /**
     * 批量提交专项练习作答
     */
    @ApiOperation("批量提交专项练习作答")
    @PreAuthorize("hasAuthority('system:review:batchSubmitPracticeAnswer')")
    @PostMapping("/batchSubmitPracticeAnswer")
    public Result<PracticeSessionDetailResp> batchSubmitPracticeAnswer(
            @RequestBody @Validated PracticeAnswerBatchSubmitReq request) {
        return ResultUtils.success(practiceAnswerSubmitService.batchSubmitPracticeAnswer(request));
    }

    /**
     * 保存专项练习草稿
     */
    @ApiOperation("保存专项练习草稿")
    @PreAuthorize("hasAuthority('system:review:savePracticeAnswerDraft')")
    @PostMapping("/savePracticeAnswerDraft")
    public Result<PracticeSessionDetailResp> savePracticeAnswerDraft(
            @RequestBody @Validated PracticeAnswerDraftSaveReq request) {
        return ResultUtils.success(practiceAnswerSubmitService.savePracticeAnswerDraft(request));
    }

    /**
     * 完成专项练习
     */
    @ApiOperation("完成专项练习")
    @PreAuthorize("hasAuthority('system:review:finishPracticeSession')")
    @PostMapping("/finishPracticeSession")
    public Result<PracticeSessionDetailResp> finishPracticeSession(
            @RequestBody @Validated PracticeSessionIdReq request) {
        return ResultUtils.success(
                practiceSessionFinishService.finishPracticeSession(request.getSessionId()));
    }

    /**
     * 专项练习历史分页列表
     */
    @ApiOperation("专项练习历史分页列表")
    @PreAuthorize("hasAuthority('system:review:practiceSessionPageList')")
    @GetMapping("/practiceSessionPageList")
    public Result<PageResult<PracticeSessionPageListResp>> practiceSessionPageList(
            @Validated PracticeSessionPageListReq request) {
        return ResultUtils.success(practiceSessionListService.practiceSessionPageList(request));
    }

    /**
     * 专项练习统计
     */
    @ApiOperation("专项练习统计")
    @PreAuthorize("hasAuthority('system:review:practiceStatistics')")
    @GetMapping("/practiceStatistics")
    public Result<PracticeStatisticsResp> practiceStatistics(
            @Validated PracticeSessionPageListReq request) {
        return ResultUtils.success(practiceStatisticsService.practiceStatistics(request));
    }

    /**
     * 查询复习计划设置
     */
    @ApiOperation("查询复习计划设置")
    @PreAuthorize("hasAuthority('system:review:reviewPlanSetting')")
    @GetMapping("/reviewPlanSetting")
    public Result<ReviewPlanSettingResp> reviewPlanSetting() {
        return ResultUtils.success(reviewPlanSettingService.reviewPlanSetting());
    }

    /**
     * 更新复习计划设置
     */
    @ApiOperation("更新复习计划设置")
    @PreAuthorize("hasAuthority('system:review:updateReviewPlanSetting')")
    @PostMapping("/updateReviewPlanSetting")
    public Result<Void> updateReviewPlanSetting(
            @RequestBody @Validated UpdateReviewPlanSettingReq request) {
        reviewPlanSettingService.updateReviewPlanSetting(request);
        return ResultUtils.success();
    }

    /**
     * 考前冲刺读取复用计划设置的已授权入口；只返回额外短练建议。
     */
    @ApiOperation("考前冲刺建议")
    @PreAuthorize("hasAuthority('system:review:reviewPlanSetting')")
    @GetMapping("/reviewExamSprint")
    public Result<ReviewExamSprintResp> reviewExamSprint(@RequestParam(required = false) String subject) {
        return ResultUtils.success(reviewExamSprintPlanService.reviewExamSprint(subject));
    }

    @ApiOperation("保存考前冲刺设置")
    @PreAuthorize("hasAuthority('system:review:updateReviewPlanSetting')")
    @PostMapping("/saveReviewExamSprint")
    public Result<ReviewExamSprintResp> saveReviewExamSprint(
            @RequestBody @Validated ReviewExamSprintSaveReq request) {
        return ResultUtils.success(reviewExamSprintPlanService.saveReviewExamSprint(request));
    }

    /**
     * 查询当前学生的教材等学习偏好。
     */
    @ApiOperation("查询学习偏好")
    @PreAuthorize("hasAuthority('system:review:learningProfile')")
    @GetMapping("/learningProfile")
    public Result<LearningProfileResp> learningProfile() {
        return ResultUtils.success(learningProfileService.learningProfile());
    }

    @ApiOperation("个人知识学习路径")
    @PreAuthorize("hasAuthority('system:review:learningPath')")
    @GetMapping("/learningPath")
    public Result<LearningPathResp> learningPath(@RequestParam(required = false) String subject) {
        return ResultUtils.success(learningPathService.learningPath(subject));
    }

    /**
     * 更新当前学生的教材等学习偏好。
     */
    @ApiOperation("更新学习偏好")
    @PreAuthorize("hasAuthority('system:review:updateLearningProfile')")
    @PostMapping("/updateLearningProfile")
    public Result<Void> updateLearningProfile(
            @RequestBody @Validated UpdateLearningProfileReq request) {
        learningProfileService.updateLearningProfile(request);
        return ResultUtils.success();
    }

    /**
     * 导出当前学生的个人学习数据备份。
     */
    @ApiOperation("导出个人学习数据备份")
    @PreAuthorize("hasAuthority('system:review:learningDataBackup')")
    @GetMapping("/learningDataBackup")
    public Result<LearningDataBackupResp> learningDataBackup() {
        return ResultUtils.success(learningDataBackupService.learningDataBackup());
    }

    /**
     * 复习历史分页列表
     */
    @ApiOperation("复习历史分页列表")
    @PreAuthorize("hasAuthority('system:review:reviewHistoryPageList')")
    @GetMapping("/reviewHistoryPageList")
    public Result<ReviewHistoryHomeResp> reviewHistoryPageList(
            @Validated ReviewHistoryPageListReq request) {
        return ResultUtils.success(reviewHistoryListService.reviewHistoryPageList(request));
    }

    /**
     * 复习提醒分页列表
     */
    @ApiOperation("复习提醒分页列表")
    @PreAuthorize("hasAuthority('system:review:reviewReminderPageList')")
    @GetMapping("/reviewReminderPageList")
    public Result<ReviewReminderHomeResp> reviewReminderPageList(
            @Validated ReviewReminderPageListReq request) {
        return ResultUtils.success(reviewReminderListService.reviewReminderPageList(request));
    }

    /**
     * 将复习提醒设为已读
     */
    @ApiOperation("将复习提醒设为已读")
    @PreAuthorize("hasAuthority('system:review:readReviewReminder')")
    @PostMapping("/readReviewReminder")
    public Result<Void> readReviewReminder(@RequestBody @Validated ReviewReminderIdReq request) {
        reviewReminderUpdateService.readReviewReminder(request.getId());
        return ResultUtils.success();
    }
}
