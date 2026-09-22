package com.study.module.system.questionbank.controller;

import com.study.common.core.domain.Result;
import com.study.common.core.utils.ResultUtils;
import com.study.common.core.domain.dto.PageResult;
import com.study.module.system.questionbank.dto.request.QuestionPracticeHistoryPageListReq;
import com.study.module.system.questionbank.dto.response.QuestionPracticeHistoryPageListResp;
import com.study.module.system.questionbank.service.QuestionPracticeHistoryListService;
import com.study.module.system.questionbank.dto.request.QuestionPracticeSubmitReq;
import com.study.module.system.questionbank.dto.request.QuestionPracticeAnswerViewReq;
import com.study.module.system.questionbank.dto.response.QuestionPracticeAnswerViewResp;
import com.study.module.system.questionbank.dto.request.SimilarQuestionListReq;
import com.study.module.system.questionbank.dto.response.QuestionPracticeSubmitResp;
import com.study.module.system.questionbank.dto.response.QuestionPracticeStatisticsResp;
import com.study.module.system.questionbank.dto.response.SimilarQuestionListResp;
import com.study.module.system.questionbank.service.QuestionPracticeStatisticsService;
import com.study.module.system.questionbank.service.QuestionPracticeSubmitService;
import com.study.module.system.questionbank.service.SimilarQuestionListService;
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
 * 题目推荐记录接口
 */
@Api(tags = "相似题推荐")
@RestController
@RequestMapping("/api/questionRecommendationLog")
public class QuestionRecommendationLogController {

    @Autowired
    SimilarQuestionListService similarQuestionListService;

    @Autowired
    QuestionPracticeSubmitService questionPracticeSubmitService;

    @Autowired
    QuestionPracticeStatisticsService questionPracticeStatisticsService;

    @Autowired
    QuestionPracticeHistoryListService questionPracticeHistoryListService;

    @ApiOperation("获取审核相似题")
    @PreAuthorize("hasAuthority('system:questionBank:similarQuestionList')")
    /**
     * 执行 similarQuestionList 业务处理。
     */
    @PostMapping("/similarQuestionList")
    public Result<List<SimilarQuestionListResp>> similarQuestionList(@RequestBody @Validated SimilarQuestionListReq request) {
        return ResultUtils.success(similarQuestionListService.similarQuestionList(request));
    }

    @ApiOperation("提交相似题作答")
    @PreAuthorize("hasAuthority('system:questionBank:submitQuestionPractice')")
    @PostMapping("/submitQuestionPractice")
    public Result<QuestionPracticeSubmitResp> submitQuestionPractice(
            @RequestBody @Validated QuestionPracticeSubmitReq request) {
        return ResultUtils.success(questionPracticeSubmitService.submitQuestionPractice(request));
    }

    @ApiOperation("主观题提交作答并查看标准答案")
    @PreAuthorize("hasAuthority('system:questionBank:viewQuestionPracticeAnswer')")
    @PostMapping("/viewQuestionPracticeAnswer")
    public Result<QuestionPracticeAnswerViewResp> viewQuestionPracticeAnswer(
            @RequestBody @Validated QuestionPracticeAnswerViewReq request) {
        return ResultUtils.success(questionPracticeSubmitService.viewQuestionPracticeAnswer(request));
    }

    @ApiOperation("相似题运营统计")
    @PreAuthorize("hasAuthority('system:questionBank:questionPracticeStatistics')")
    /**
     * 查询题目练习统计数据。
     */
    @GetMapping("/questionPracticeStatistics")
    public Result<QuestionPracticeStatisticsResp> questionPracticeStatistics() {
        return ResultUtils.success(questionPracticeStatisticsService.questionPracticeStatistics());
    }

    @ApiOperation("相似题练习历史明细")
    @PreAuthorize("hasAuthority('system:questionBank:questionPracticeHistoryPageList')")
    @GetMapping("/questionPracticeHistoryPageList")
    public Result<PageResult<QuestionPracticeHistoryPageListResp>> questionPracticeHistoryPageList(
            @Validated QuestionPracticeHistoryPageListReq request) {
        return ResultUtils.success(questionPracticeHistoryListService.questionPracticeHistoryPageList(request));
    }
}
