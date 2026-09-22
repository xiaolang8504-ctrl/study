package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.api.contants.UploadType;
import com.study.api.dto.response.FileData;
import com.study.api.provider.FileProvider;
import com.study.common.core.domain.dto.PageResult;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.review.service.ReviewEnrollmentService;
import com.study.module.system.dict.service.DictDataService;
import com.study.module.system.file.service.FileService;
import com.study.module.system.wrongquestion.constants.QuestionCaptureFileType;
import com.study.module.system.wrongquestion.constants.WrongQuestionDictType;
import com.study.module.system.wrongquestion.convert.WrongQuestionConvert;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureConfirmReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureDuplicateCheckReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionIdsReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionCreateReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionSplitReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionSnapshotReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionSnapshotItemReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureRegionUpdateReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureTaskCreateReq;
import com.study.module.system.wrongquestion.dto.request.QuestionCaptureTaskPageListReq;
import com.study.module.system.wrongquestion.dto.response.QuestionCapturePageResp;
import com.study.module.system.wrongquestion.dto.response.QuestionCaptureDuplicateResp;
import com.study.module.system.wrongquestion.dto.response.QuestionCaptureRegionResp;
import com.study.module.system.wrongquestion.dto.response.QuestionCaptureTaskPageListResp;
import com.study.module.system.wrongquestion.dto.response.QuestionCaptureTaskResp;
import com.study.module.system.wrongquestion.entity.QuestionCapturePage;
import com.study.module.system.wrongquestion.entity.QuestionCaptureRegion;
import com.study.module.system.wrongquestion.entity.QuestionCaptureTask;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import com.study.module.system.wrongquestion.service.QuestionCaptureOcrService;
import com.study.module.system.wrongquestion.service.QuestionCapturePageService;
import com.study.module.system.wrongquestion.service.QuestionCapturePdfService;
import com.study.module.system.wrongquestion.service.QuestionCaptureRegionService;
import com.study.module.system.wrongquestion.service.QuestionCaptureService;
import com.study.module.system.wrongquestion.service.QuestionCaptureTaskService;
import com.study.module.system.wrongquestion.service.QuestionCaptureEventService;
import com.study.module.system.wrongquestion.service.WrongQuestionService;
import com.study.module.system.wrongquestion.service.WrongQuestionTimelineService;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 题目采集任务、页面与题块确认的业务编排。
 */
@Service
public class QuestionCaptureServiceImpl implements QuestionCaptureService {

    private static final int TASK_PROCESSING = 1;
    private static final int TASK_WAITING_CONFIRM = 2;
    private static final int TASK_FAILED = 4;
    private static final int REGION_WAITING_CONFIRM = 0;
    private static final int REGION_CONFIRMED = 1;
    private static final int REGION_SKIPPED = 2;

    @Autowired
    private QuestionCaptureTaskService questionCaptureTaskService;
    @Autowired
    private QuestionCapturePageService questionCapturePageService;
    @Autowired
    private QuestionCaptureRegionService questionCaptureRegionService;
    @Autowired
    private QuestionCaptureOcrService questionCaptureOcrService;
    @Autowired
    private QuestionCapturePdfService questionCapturePdfService;
    @DubboReference
    private FileProvider fileProvider;
    @Autowired
    private FileService fileService;
    @Autowired
    private WrongQuestionService wrongQuestionService;
    @Autowired
    private ReviewEnrollmentService reviewEnrollmentService;
    @Autowired
    private DictDataService dictDataService;
    @Autowired
    private QuestionCaptureEventService questionCaptureEventService;
    @Autowired
    private WrongQuestionTimelineService wrongQuestionTimelineService;

    /**
     * 创建或保存题目采集。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuestionCaptureTask(QuestionCaptureTaskCreateReq request) {
        return createQuestionCaptureTaskByUserId(request, AccountUtils.getUserId());
    }

    /**
     * 兼容免鉴权旧入口：由控制层解析 JWT 后显式传入任务归属用户。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuestionCaptureTaskByUserId(QuestionCaptureTaskCreateReq request, Long userId) {
        if (userId == null) {
            throw new LogicException(ErrorCodeConstants.NO_LOGIN);
        }
        String clientRequestId = trimToNull(request.getClientRequestId());
        if (clientRequestId != null) {
            QuestionCaptureTask existedTask = questionCaptureTaskService.getOne(new LambdaQueryWrapper<QuestionCaptureTask>()
                    .eq(QuestionCaptureTask::getCreateId, userId)
                    .eq(QuestionCaptureTask::getClientRequestId, clientRequestId));
            if (existedTask != null) {
                return existedTask.getId();
            }
        }
        List<FileData> sourceFileList = checkCaptureSourceFiles(request.getImageFileIds(), userId);
        LocalDateTime now = LocalDateTime.now();
        QuestionCaptureTask task = WrongQuestionConvert.INSTANCE.toQuestionCaptureTask(request);
        task.setGrade(resolveDictValue(WrongQuestionDictType.GRADE, request.getGrade()));
        task.setSubject(resolveDictValue(WrongQuestionDictType.SUBJECT, request.getSubject()));
        task.setQuestionType(resolveDictValue(WrongQuestionDictType.QUESTION_TYPE, request.getQuestionType()));
        task.setSource(resolveDictValue(WrongQuestionDictType.SOURCE, request.getSource()));
        task.setStatus(0);
        task.setRetryCount(0);
        task.setClientRequestId(clientRequestId);
        task.setCreateId(userId);
        task.setCreateTime(now);
        task.setUpdateTime(now);
        try {
            if (!questionCaptureTaskService.save(task)) {
                throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
            }
        } catch (DuplicateKeyException duplicate) {
            if (clientRequestId == null) {
                throw duplicate;
            }
            // MySQL 唯一索引是最终仲裁；FOR UPDATE 使用当前读，避免事务快照看不到并发赢家。
            QuestionCaptureTask winner = questionCaptureTaskService.getOne(new LambdaQueryWrapper<QuestionCaptureTask>()
                    .eq(QuestionCaptureTask::getCreateId, userId)
                    .eq(QuestionCaptureTask::getClientRequestId, clientRequestId)
                    .last("FOR UPDATE"));
            if (winner == null) {
                throw duplicate;
            }
            return winner.getId();
        }

        int pageNo = 1;
        List<Long> pdfFileIds = new ArrayList<>();
        for (int index = 0; index < request.getImageFileIds().size(); index++) {
            Long fileId = request.getImageFileIds().get(index);
            FileData fileData = sourceFileList.get(index);
            if (QuestionCaptureFileType.isPdf(fileData.getFileExtension())) {
                pdfFileIds.add(fileId);
                continue;
            }
            QuestionCapturePage page = new QuestionCapturePage();
            page.setTaskId(task.getId());
            page.setImageFileId(fileId);
            page.setSourceFileId(fileId);
            page.setSourcePageNo(1);
            page.setPageNo(pageNo++);
            page.setStatus(0);
            page.setRetryCount(0);
            page.setCleanStatus(0);
            page.setCreateTime(now);
            page.setUpdateTime(now);
            questionCapturePageService.save(page);
        }
        questionCaptureEventService.record(task.getId(), null, null, userId, null, "TASK_CREATED", "SUCCESS",
                null, null, 0, null, null);
        if (pdfFileIds.isEmpty()) {
            dispatchAfterCommit(() -> questionCaptureOcrService.recognizeQuestionCaptureTask(task.getId()));
        } else {
            dispatchAfterCommit(() -> questionCapturePdfService.renderQuestionCapturePdfs(task.getId(), pdfFileIds));
        }
        return task.getId();
    }

    /**
     * 创建采集任务前校验来源文件归属，禁止通过枚举文件 ID 引用其他学生的私有文件。
     */
    private List<FileData> checkCaptureSourceFiles(List<Long> fileIds, Long userId) {
        List<FileData> sourceFileList = new ArrayList<>();
        for (Long fileId : fileIds) {
            fileService.checkUserFile(Math.toIntExact(fileId), UploadType.WRONG_QUESTION, userId);
            FileData fileData = fileProvider.getFile(UploadType.WRONG_QUESTION, Math.toIntExact(fileId));
            if (fileData == null || !QuestionCaptureFileType.isSupported(fileData.getFileExtension())) {
                throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
            }
            sourceFileList.add(fileData);
        }
        return sourceFileList;
    }

    /**
     * 分页查询题目采集。
     */
    @Override
    public PageResult<QuestionCaptureTaskPageListResp> questionCaptureTaskPageList(QuestionCaptureTaskPageListReq request) {
        Page<QuestionCaptureTask> page = new Page<>(request.getCurrent(), request.getPageSize());
        questionCaptureTaskService.page(page, new LambdaQueryWrapper<QuestionCaptureTask>()
                .eq(QuestionCaptureTask::getCreateId, AccountUtils.getUserId())
                .eq(request.getStatus() != null, QuestionCaptureTask::getStatus, request.getStatus())
                .orderByDesc(QuestionCaptureTask::getId));
        return PageUtils.wrap(page, rows -> rows.stream().map(item -> {
            return WrongQuestionConvert.INSTANCE.toQuestionCaptureTaskPageListResp(item);
        }).collect(Collectors.toList()));
    }

    /**
     * 查询题目采集详情。
     */
    @Override
    public QuestionCaptureTaskResp questionCaptureTaskDetail(Long id) {
        QuestionCaptureTask task = checkTask(id);
        QuestionCaptureTaskResp response = WrongQuestionConvert.INSTANCE.toQuestionCaptureTaskResp(task);
        response.setRegionList(questionCaptureRegionService.list(new LambdaQueryWrapper<QuestionCaptureRegion>()
                        .eq(QuestionCaptureRegion::getTaskId, id)
                        .orderByAsc(QuestionCaptureRegion::getPageId, QuestionCaptureRegion::getRegionNo))
                .stream().map(region -> {
                    return WrongQuestionConvert.INSTANCE.toQuestionCaptureRegionResp(region);
                }).collect(Collectors.toList()));
        List<QuestionCapturePage> pageList = questionCapturePageService.list(new LambdaQueryWrapper<QuestionCapturePage>()
                        .eq(QuestionCapturePage::getTaskId, id)
                        .orderByAsc(QuestionCapturePage::getPageNo));
        response.setPageList(pageList.stream().map(page -> {
                    return WrongQuestionConvert.INSTANCE.toQuestionCapturePageResp(page);
                }).collect(Collectors.toList()));
        response.setTotalPageCount(pageList.size());
        response.setCompletedPageCount((int) pageList.stream().filter(item -> Integer.valueOf(2).equals(item.getStatus())).count());
        response.setFailedPageCount((int) pageList.stream().filter(item -> Integer.valueOf(TASK_FAILED).equals(item.getStatus())).count());
        response.setProcessingPageCount((int) pageList.stream().filter(item -> Integer.valueOf(0).equals(item.getStatus())
                || Integer.valueOf(TASK_PROCESSING).equals(item.getStatus())).count());
        return response;
    }

    /**
     * 重新处理题目采集。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryQuestionCaptureTask(Long id) {
        QuestionCaptureTask task = checkTask(id);
        if (!Integer.valueOf(TASK_FAILED).equals(task.getStatus()) && !Integer.valueOf(TASK_WAITING_CONFIRM).equals(task.getStatus())) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        int confirmedCount = Math.toIntExact(questionCaptureRegionService.count(new LambdaQueryWrapper<QuestionCaptureRegion>()
                .eq(QuestionCaptureRegion::getTaskId, id)
                .eq(QuestionCaptureRegion::getStatus, REGION_CONFIRMED)));
        if (confirmedCount > 0) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        questionCaptureRegionService.remove(new LambdaQueryWrapper<QuestionCaptureRegion>().eq(QuestionCaptureRegion::getTaskId, id));
        task.setStatus(0);
        task.setFailReason(null);
        task.setRetryCount(defaultValue(task.getRetryCount()) + 1);
        task.setUpdateTime(LocalDateTime.now());
        questionCaptureTaskService.updateById(task);
        questionCaptureEventService.record(task.getId(), null, null, task.getCreateId(), null, "TASK_RETRIED", "INFO",
                null, null, null, null, null);
        dispatchAfterCommit(() -> questionCaptureOcrService.recognizeQuestionCaptureTask(id));
    }

    /**
     * 更新题目采集。
     */
    @Override
    public void updateQuestionCaptureRegion(QuestionCaptureRegionUpdateReq request) {
        QuestionCaptureRegion region = checkWaitingRegion(request.getId());
        validateRegionPosition(request.getLeftPosition(), request.getTopPosition(), request.getWidth(), request.getHeight());
        region.setQuestionTitle(request.getQuestionTitle());
        region.setQuestionContent(request.getQuestionContent());
        region.setWrongAnswer(request.getWrongAnswer());
        region.setCorrectAnswer(request.getCorrectAnswer());
        region.setWrongReason(request.getWrongReason());
        region.setAnalysis(request.getAnalysis());
        region.setGrade(request.getGrade());
        region.setSubject(request.getSubject());
        region.setQuestionType(request.getQuestionType());
        region.setSource(request.getSource());
        region.setLearningPoint(request.getLearningPoint());
        region.setErrorLabels(request.getErrorLabels());
        region.setLeftPosition(request.getLeftPosition());
        region.setTopPosition(request.getTopPosition());
        region.setWidth(request.getWidth());
        region.setHeight(request.getHeight());
        region.setManuallyCorrected(1);
        region.setUpdateTime(LocalDateTime.now());
        questionCaptureRegionService.updateById(region);
        questionCaptureEventService.record(region.getTaskId(), region.getPageId(), region.getId(),
                AccountUtils.getUserId(), null, "REGION_UPDATED", "SUCCESS", null, null, null, null, null);
    }

    /**
     * 人工补充 OCR 漏掉的题块。
     */
    @Override
    public void createQuestionCaptureRegion(QuestionCaptureRegionCreateReq request) {
        ensureTaskWaitingConfirm(checkTask(request.getTaskId()));
        QuestionCapturePage page = questionCapturePageService.getById(request.getPageId());
        if (page == null || !request.getTaskId().equals(page.getTaskId())) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        validateRegionPosition(request.getLeftPosition(), request.getTopPosition(), request.getWidth(), request.getHeight());
        int maxRegionNo = questionCaptureRegionService.list(new LambdaQueryWrapper<QuestionCaptureRegion>()
                        .eq(QuestionCaptureRegion::getPageId, page.getId()))
                .stream().map(QuestionCaptureRegion::getRegionNo).filter(item -> item != null)
                .max(Integer::compareTo).orElse(0);
        LocalDateTime now = LocalDateTime.now();
        QuestionCaptureRegion region = new QuestionCaptureRegion();
        region.setTaskId(request.getTaskId());
        region.setPageId(page.getId());
        region.setRegionNo(maxRegionNo + 1);
        region.setConfidence(0);
        region.setQuestionTitleConfidence(0);
        region.setQuestionContentConfidence(0);
        region.setWrongAnswerConfidence(0);
        region.setCorrectAnswerConfidence(0);
        region.setAnalysisConfidence(0);
        region.setLeftPosition(request.getLeftPosition());
        region.setTopPosition(request.getTopPosition());
        region.setWidth(request.getWidth());
        region.setHeight(request.getHeight());
        region.setManuallyCorrected(1);
        region.setStatus(REGION_WAITING_CONFIRM);
        region.setCreateTime(now);
        region.setUpdateTime(now);
        questionCaptureRegionService.save(region);
        questionCaptureEventService.record(request.getTaskId(), page.getId(), region.getId(), AccountUtils.getUserId(),
                null, "REGION_CREATED", "SUCCESS", null, null, 1, null, null);
    }

    /**
     * 合并题目采集。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mergeQuestionCaptureRegion(QuestionCaptureRegionIdsReq request) {
        ensureTaskWaitingConfirm(checkTask(request.getTaskId()));
        List<QuestionCaptureRegion> regions = questionCaptureRegionService.list(new LambdaQueryWrapper<QuestionCaptureRegion>()
                .eq(QuestionCaptureRegion::getTaskId, request.getTaskId())
                .in(QuestionCaptureRegion::getId, request.getRegionIds())
                .eq(QuestionCaptureRegion::getStatus, REGION_WAITING_CONFIRM));
        if (regions.size() != request.getRegionIds().size() || regions.size() < 2) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        Long pageId = regions.get(0).getPageId();
        if (regions.stream().anyMatch(item -> !pageId.equals(item.getPageId()))) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        regions.sort(Comparator.comparing(QuestionCaptureRegion::getRegionNo));
        QuestionCaptureRegion target = regions.get(0);
        int left = regions.stream().map(QuestionCaptureRegion::getLeftPosition).filter(item -> item != null).min(Integer::compareTo).orElse(0);
        int top = regions.stream().map(QuestionCaptureRegion::getTopPosition).filter(item -> item != null).min(Integer::compareTo).orElse(0);
        int right = regions.stream().map(item -> defaultValue(item.getLeftPosition()) + defaultValue(item.getWidth(), 10000)).max(Integer::compareTo).orElse(10000);
        int bottom = regions.stream().map(item -> defaultValue(item.getTopPosition()) + defaultValue(item.getHeight(), 10000)).max(Integer::compareTo).orElse(10000);
        target.setQuestionContent(regions.stream().map(QuestionCaptureRegion::getQuestionContent).filter(item -> item != null).collect(Collectors.joining("\n")));
        target.setQuestionTitle(regions.stream().map(QuestionCaptureRegion::getQuestionTitle).filter(item -> item != null).findFirst().orElse(target.getQuestionTitle()));
        target.setLeftPosition(left);
        target.setTopPosition(top);
        target.setWidth(Math.min(10000 - left, right - left));
        target.setHeight(Math.min(10000 - top, bottom - top));
        target.setManuallyCorrected(1);
        target.setUpdateTime(LocalDateTime.now());
        questionCaptureRegionService.updateById(target);
        for (QuestionCaptureRegion region : regions.subList(1, regions.size())) {
            region.setStatus(REGION_SKIPPED);
            region.setManuallyCorrected(1);
            region.setUpdateTime(LocalDateTime.now());
            questionCaptureRegionService.updateById(region);
        }
        questionCaptureEventService.record(request.getTaskId(), pageId, target.getId(), AccountUtils.getUserId(), null,
                "REGIONS_MERGED", "SUCCESS", null, null, regions.size(), null, null);
    }

    /**
     * 拆分题目采集。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void splitQuestionCaptureRegion(Long id) {
        QuestionCaptureRegionSplitReq request = new QuestionCaptureRegionSplitReq();
        request.setId(id);
        request.setSplitRatio(50);
        splitQuestionCaptureRegion(request);
    }

    /**
     * 按用户指定比例垂直切开题块，避免固定二等分导致题干或答案被截断。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void splitQuestionCaptureRegion(QuestionCaptureRegionSplitReq request) {
        QuestionCaptureRegion origin = checkWaitingRegion(request.getId());
        int originTop = defaultValue(origin.getTopPosition());
        int originHeight = defaultValue(origin.getHeight(), 10000);
        int firstHeight = Math.round(originHeight * request.getSplitRatio() / 100F);
        int secondHeight = originHeight - firstHeight;
        if (firstHeight < 100 || secondHeight < 100) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        int maxRegionNo = questionCaptureRegionService.list(new LambdaQueryWrapper<QuestionCaptureRegion>()
                        .eq(QuestionCaptureRegion::getPageId, origin.getPageId()))
                .stream().map(QuestionCaptureRegion::getRegionNo).filter(item -> item != null).max(Integer::compareTo).orElse(0);
        QuestionCaptureRegion copy = WrongQuestionConvert.INSTANCE.copyQuestionCaptureRegion(origin);
        copy.setRegionNo(maxRegionNo + 1);
        copy.setTopPosition(originTop + firstHeight);
        copy.setHeight(secondHeight);
        copy.setManuallyCorrected(1);
        copy.setCreateTime(LocalDateTime.now());
        copy.setUpdateTime(LocalDateTime.now());
        questionCaptureRegionService.save(copy);
        origin.setHeight(firstHeight);
        origin.setManuallyCorrected(1);
        origin.setUpdateTime(LocalDateTime.now());
        questionCaptureRegionService.updateById(origin);
        questionCaptureEventService.record(origin.getTaskId(), origin.getPageId(), origin.getId(), AccountUtils.getUserId(),
                null, "REGION_SPLIT", "SUCCESS", null, null, 2, null, null);
    }

    /**
     * 用浏览器操作前保存的快照恢复题块，仅允许任务尚未确认任何错题时执行。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void restoreQuestionCaptureRegionSnapshot(QuestionCaptureRegionSnapshotReq request) {
        QuestionCaptureTask task = checkTask(request.getTaskId());
        ensureTaskWaitingConfirm(task);
        long confirmedCount = questionCaptureRegionService.count(new LambdaQueryWrapper<QuestionCaptureRegion>()
                .eq(QuestionCaptureRegion::getTaskId, task.getId())
                .eq(QuestionCaptureRegion::getStatus, REGION_CONFIRMED));
        if (confirmedCount > 0) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        java.util.Set<Long> pageIds = questionCapturePageService.list(new LambdaQueryWrapper<QuestionCapturePage>()
                        .eq(QuestionCapturePage::getTaskId, task.getId()))
                .stream().map(QuestionCapturePage::getId).collect(Collectors.toSet());
        if (request.getRegionList().stream().anyMatch(item -> !pageIds.contains(item.getPageId())
                || item.getStatus() == null || item.getStatus() < REGION_WAITING_CONFIRM || item.getStatus() > REGION_SKIPPED)) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        LocalDateTime now = LocalDateTime.now();
        questionCaptureRegionService.remove(new LambdaQueryWrapper<QuestionCaptureRegion>()
                .eq(QuestionCaptureRegion::getTaskId, task.getId()));
        questionCaptureRegionService.saveBatch(request.getRegionList().stream()
                .map(item -> toSnapshotRegion(task.getId(), item, now)).collect(Collectors.toList()));
        task.setUpdateTime(now);
        questionCaptureTaskService.updateById(task);
        questionCaptureEventService.record(task.getId(), null, null, AccountUtils.getUserId(), null,
                "REGION_SNAPSHOT_RESTORED", "SUCCESS", null, null, request.getRegionList().size(), null, null);
    }

    /**
     * 删除题目采集。
     */
    @Override
    public void deleteQuestionCaptureRegion(Long id) {
        QuestionCaptureRegion region = checkWaitingRegion(id);
        region.setStatus(REGION_SKIPPED);
        region.setManuallyCorrected(1);
        region.setUpdateTime(LocalDateTime.now());
        questionCaptureRegionService.updateById(region);
        questionCaptureEventService.record(region.getTaskId(), region.getPageId(), region.getId(), AccountUtils.getUserId(),
                null, "REGION_SKIPPED", "SUCCESS", null, null, null, null, null);
    }

    /**
     * 执行 restoreQuestionCaptureRegion 业务处理。
     */
    @Override
    public void restoreQuestionCaptureRegion(Long id) {
        QuestionCaptureRegion region = questionCaptureRegionService.getById(id);
        if (region == null || !Integer.valueOf(REGION_SKIPPED).equals(region.getStatus())) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        ensureTaskWaitingConfirm(checkTask(region.getTaskId()));
        region.setStatus(REGION_WAITING_CONFIRM);
        region.setManuallyCorrected(1);
        region.setUpdateTime(LocalDateTime.now());
        questionCaptureRegionService.updateById(region);
        questionCaptureEventService.record(region.getTaskId(), region.getPageId(), region.getId(), AccountUtils.getUserId(),
                null, "REGION_RESTORED", "SUCCESS", null, null, null, null, null);
    }

    /**
     * 重新处理题目采集。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryQuestionCapturePage(Long id) {
        QuestionCapturePage page = questionCapturePageService.getById(id);
        if (page == null) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        QuestionCaptureTask task = checkTask(page.getTaskId());
        if (!Integer.valueOf(TASK_WAITING_CONFIRM).equals(task.getStatus())
                && !Integer.valueOf(TASK_FAILED).equals(task.getStatus())) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        // PDF 渲染失败时保留的是来源文件占位页；删除占位页后重新提交该 PDF 分页，避免把 PDF 当成图片送入 OCR。
        if (page.getSourcePageNo() == null && page.getSourceFileId() != null && page.getSourceFileId().equals(page.getImageFileId())) {
            questionCapturePageService.removeById(page.getId());
            task.setStatus(TASK_PROCESSING);
            task.setFailReason(null);
            task.setUpdateTime(LocalDateTime.now());
            questionCaptureTaskService.updateById(task);
            dispatchAfterCommit(() -> questionCapturePdfService.renderQuestionCapturePdfs(task.getId(),
                    java.util.Collections.singletonList(page.getSourceFileId())));
            questionCaptureEventService.record(task.getId(), page.getId(), null, AccountUtils.getUserId(), page.getSourceFileId(),
                    "PDF_PAGE_RETRIED", "INFO", null, null, null, null, null);
            return;
        }
        questionCaptureRegionService.remove(new LambdaQueryWrapper<QuestionCaptureRegion>()
                .eq(QuestionCaptureRegion::getPageId, id)
                .eq(QuestionCaptureRegion::getStatus, REGION_WAITING_CONFIRM));
        page.setStatus(0);
        page.setFailReason(null);
        page.setRetryCount(defaultValue(page.getRetryCount()) + 1);
        page.setUpdateTime(LocalDateTime.now());
        questionCapturePageService.updateById(page);
        task.setStatus(TASK_PROCESSING);
        task.setFailReason(null);
        task.setUpdateTime(LocalDateTime.now());
        questionCaptureTaskService.updateById(task);
        dispatchAfterCommit(() -> questionCaptureOcrService.recognizeQuestionCapturePage(id));
        questionCaptureEventService.record(task.getId(), page.getId(), null, AccountUtils.getUserId(), page.getImageFileId(),
                "PAGE_RETRIED", "INFO", null, null, null, null, null);
    }

    /**
     * OCR 或 PDF 分页失败时，保留页面原图并生成一个整页人工题块。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestionCapturePageAsImage(Long id) {
        QuestionCapturePage page = questionCapturePageService.getById(id);
        if (page == null) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        QuestionCaptureTask task = checkTask(page.getTaskId());
        if (Integer.valueOf(TASK_PROCESSING).equals(task.getStatus())) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        long pendingCount = questionCaptureRegionService.count(new LambdaQueryWrapper<QuestionCaptureRegion>()
                .eq(QuestionCaptureRegion::getPageId, page.getId())
                .eq(QuestionCaptureRegion::getStatus, REGION_WAITING_CONFIRM));
        if (pendingCount > 0) {
            return;
        }
        int maxRegionNo = questionCaptureRegionService.list(new LambdaQueryWrapper<QuestionCaptureRegion>()
                        .eq(QuestionCaptureRegion::getPageId, page.getId()))
                .stream().map(QuestionCaptureRegion::getRegionNo).filter(item -> item != null)
                .max(Integer::compareTo).orElse(0);
        LocalDateTime now = LocalDateTime.now();
        QuestionCaptureRegion region = new QuestionCaptureRegion();
        region.setTaskId(task.getId());
        region.setPageId(page.getId());
        region.setRegionNo(maxRegionNo + 1);
        region.setConfidence(0);
        region.setQuestionTitleConfidence(0);
        region.setQuestionContentConfidence(0);
        region.setWrongAnswerConfidence(0);
        region.setCorrectAnswerConfidence(0);
        region.setAnalysisConfidence(0);
        region.setLeftPosition(0);
        region.setTopPosition(0);
        region.setWidth(10000);
        region.setHeight(10000);
        region.setQuestionTitle("仅保存图片错题");
        region.setManuallyCorrected(1);
        region.setStatus(REGION_WAITING_CONFIRM);
        region.setCreateTime(now);
        region.setUpdateTime(now);
        questionCaptureRegionService.save(region);
        task.setStatus(TASK_WAITING_CONFIRM);
        task.setFailReason("已保留原图，请补充题目内容后确认创建错题");
        task.setUpdateTime(now);
        questionCaptureTaskService.updateById(task);
        questionCaptureEventService.record(task.getId(), page.getId(), region.getId(), AccountUtils.getUserId(),
                page.getImageFileId(), "PAGE_SAVED_AS_IMAGE", "SUCCESS", null, null, 1, null, null);
    }

    /**
     * 检查当前学生是否已经收录了同年级、同科目且规范化题干一致的错题。
     */
    @Override
    public List<QuestionCaptureDuplicateResp> questionCaptureDuplicateList(QuestionCaptureDuplicateCheckReq request) {
        QuestionCaptureTask task = checkTask(request.getTaskId());
        ensureTaskWaitingConfirm(task);
        List<QuestionCaptureRegion> regions = questionCaptureRegionService.list(new LambdaQueryWrapper<QuestionCaptureRegion>()
                .eq(QuestionCaptureRegion::getTaskId, task.getId())
                .in(QuestionCaptureRegion::getId, request.getRegionIds())
                .eq(QuestionCaptureRegion::getStatus, REGION_WAITING_CONFIRM));
        if (regions.size() != request.getRegionIds().size()) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        Long userId = AccountUtils.getUserId();
        List<QuestionCaptureDuplicateResp> responseList = new ArrayList<>();
        Set<String> checkedScopeSet = new LinkedHashSet<>();
        Map<String, List<WrongQuestion>> existingQuestionMap = new java.util.HashMap<>();
        for (QuestionCaptureRegion region : regions) {
            String grade = firstNotBlank(region.getGrade(), task.getGrade());
            String subject = firstNotBlank(region.getSubject(), task.getSubject());
            String scopeKey = grade + "#" + subject;
            if (checkedScopeSet.add(scopeKey)) {
                List<WrongQuestion> existingList = wrongQuestionService.list(new LambdaQueryWrapper<WrongQuestion>()
                        .eq(WrongQuestion::getCreateId, userId)
                        .eq(WrongQuestion::getGrade, grade)
                        .eq(WrongQuestion::getSubject, subject));
                existingQuestionMap.put(scopeKey, existingList);
            }
            String regionContent = trimToNull(region.getQuestionContent());
            if ("仅保存图片错题".equals(trimToNull(region.getQuestionTitle()))) {
                continue;
            }
            String fingerprint = questionFingerprint(firstNotBlank(regionContent, region.getQuestionTitle()));
            if (fingerprint.isEmpty()) {
                continue;
            }
            for (WrongQuestion question : existingQuestionMap.getOrDefault(scopeKey, Collections.emptyList())) {
                if (!fingerprint.equals(questionFingerprint(firstNotBlank(question.getQuestionContent(), question.getQuestionTitle())))) {
                    continue;
                }
                QuestionCaptureDuplicateResp duplicate = new QuestionCaptureDuplicateResp();
                duplicate.setRegionId(region.getId());
                duplicate.setWrongQuestionId(question.getId());
                duplicate.setQuestionTitle(question.getQuestionTitle());
                duplicate.setSubject(question.getSubjectName());
                duplicate.setCreateTime(question.getCreateTime());
                duplicate.setMatchType("EXACT_TEXT");
                responseList.add(duplicate);
            }
        }
        return responseList;
    }

    /**
     * 执行 confirmQuestionCapture 业务处理。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer confirmQuestionCapture(QuestionCaptureConfirmReq request) {
        QuestionCaptureTask task = checkTask(request.getTaskId());
        ensureTaskWaitingConfirm(task);
        List<QuestionCaptureRegion> regions = questionCaptureRegionService.list(new LambdaQueryWrapper<QuestionCaptureRegion>()
                .eq(QuestionCaptureRegion::getTaskId, task.getId())
                .in(QuestionCaptureRegion::getId, request.getRegionIds())
                .eq(QuestionCaptureRegion::getStatus, REGION_WAITING_CONFIRM));
        if (regions.size() != request.getRegionIds().size()) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        LocalDateTime now = LocalDateTime.now();
        Long userId = AccountUtils.getUserId();
        for (QuestionCaptureRegion region : regions) {
            int changed = questionCaptureRegionService.confirmWaitingRegion(region.getId(), now);
            if (changed != 1) {
                throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
            }
            WrongQuestion question = createWrongQuestion(task, region, userId, now);
            wrongQuestionService.fillDictNames(question);
            wrongQuestionService.save(question);
            wrongQuestionTimelineService.record(question.getId(), "CAPTURE_CONFIRMED", "OCR",
                    "已由采集题块确认创建，OCR 结果已由学生确认", userId);
            region.setStatus(REGION_CONFIRMED);
            region.setWrongQuestionId(question.getId());
            region.setUpdateTime(now);
            questionCaptureRegionService.updateById(region);
            reviewEnrollmentService.syncWrongQuestionReview(question);
        }
        int remain = Math.toIntExact(questionCaptureRegionService.count(new LambdaQueryWrapper<QuestionCaptureRegion>()
                .eq(QuestionCaptureRegion::getTaskId, task.getId())
                .eq(QuestionCaptureRegion::getStatus, REGION_WAITING_CONFIRM)));
        if (remain == 0) {
            task.setStatus(3);
            task.setUpdateTime(now);
            questionCaptureTaskService.updateById(task);
        }
        questionCaptureEventService.record(task.getId(), null, null, userId, null, "REGIONS_CONFIRMED", "SUCCESS",
                null, null, regions.size(), null, null);
        return regions.size();
    }

    /**
     * 创建业务数据。
     */
    private WrongQuestion createWrongQuestion(QuestionCaptureTask task, QuestionCaptureRegion region, Long userId, LocalDateTime now) {
        WrongQuestion question = new WrongQuestion();
        question.setGrade(resolveDictValue(WrongQuestionDictType.GRADE, firstNotBlank(region.getGrade(), task.getGrade())));
        question.setSubject(resolveDictValue(WrongQuestionDictType.SUBJECT, firstNotBlank(region.getSubject(), task.getSubject())));
        question.setQuestionType(resolveDictValue(WrongQuestionDictType.QUESTION_TYPE, firstNotBlank(region.getQuestionType(), task.getQuestionType())));
        question.setSource(resolveDictValue(WrongQuestionDictType.SOURCE, firstNotBlank(region.getSource(), task.getSource())));
        question.setLearningPoint(firstNotBlank(region.getLearningPoint(), task.getLearningPoint()));
        question.setErrorLabels(firstNotBlank(region.getErrorLabels(), task.getErrorLabels()));
        question.setStatus(0);
        question.setQuestionTitle(emptyDefault(region.getQuestionTitle(), "图片识别错题"));
        question.setQuestionContent(emptyDefault(region.getQuestionContent(), region.getQuestionTitle()));
        question.setWrongAnswer(region.getWrongAnswer());
        question.setCorrectAnswer(region.getCorrectAnswer());
        question.setWrongReason(region.getWrongReason());
        question.setAnalysis(region.getAnalysis());
        QuestionCapturePage capturePage = questionCapturePageService.getById(region.getPageId());
        if (capturePage != null && capturePage.getImageFileId() != null) {
            question.setImageUrl(String.valueOf(capturePage.getImageFileId()));
            question.setCaptureTaskId(task.getId());
            question.setCapturePageId(capturePage.getId());
            question.setCaptureRegionId(region.getId());
            question.setCaptureSourcePageNo(capturePage.getSourcePageNo());
            question.setCaptureLeftPosition(region.getLeftPosition());
            question.setCaptureTopPosition(region.getTopPosition());
            question.setCaptureWidth(region.getWidth());
            question.setCaptureHeight(region.getHeight());
        }
        question.setCreateId(userId);
        question.setCreateTime(now);
        question.setUpdateTime(now);
        return question;
    }

    private QuestionCaptureRegion toSnapshotRegion(Long taskId, QuestionCaptureRegionSnapshotItemReq item, LocalDateTime now) {
        QuestionCaptureRegion region = new QuestionCaptureRegion();
        region.setId(item.getId());
        region.setTaskId(taskId);
        region.setPageId(item.getPageId());
        region.setRegionNo(item.getRegionNo());
        region.setStatus(item.getStatus());
        region.setConfidence(defaultValue(item.getConfidence(), 0));
        region.setQuestionTitleConfidence(defaultValue(item.getQuestionTitleConfidence(), 0));
        region.setQuestionContentConfidence(defaultValue(item.getQuestionContentConfidence(), 0));
        region.setWrongAnswerConfidence(defaultValue(item.getWrongAnswerConfidence(), 0));
        region.setCorrectAnswerConfidence(defaultValue(item.getCorrectAnswerConfidence(), 0));
        region.setAnalysisConfidence(defaultValue(item.getAnalysisConfidence(), 0));
        region.setManuallyCorrected(defaultValue(item.getManuallyCorrected(), 1));
        region.setLeftPosition(item.getLeftPosition());
        region.setTopPosition(item.getTopPosition());
        region.setWidth(item.getWidth());
        region.setHeight(item.getHeight());
        region.setQuestionTitle(item.getQuestionTitle());
        region.setQuestionContent(item.getQuestionContent());
        region.setWrongAnswer(item.getWrongAnswer());
        region.setCorrectAnswer(item.getCorrectAnswer());
        region.setWrongReason(item.getWrongReason());
        region.setAnalysis(item.getAnalysis());
        region.setGrade(item.getGrade());
        region.setSubject(item.getSubject());
        region.setQuestionType(item.getQuestionType());
        region.setSource(item.getSource());
        region.setLearningPoint(item.getLearningPoint());
        region.setErrorLabels(item.getErrorLabels());
        region.setCreateTime(now);
        region.setUpdateTime(now);
        return region;
    }

    /**
     * 采集入口兼容字典键值与展示名称，落库始终使用字典键值。
     */
    private String resolveDictValue(String dictType, String value) {
        Map<String, String> dictValueLabelMap = dictDataService.dictDataIdMap(dictType);
        if (dictValueLabelMap.containsKey(value)) {
            return value;
        }
        return dictValueLabelMap.entrySet().stream()
                .filter(item -> item.getValue().equals(value))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new LogicException(ErrorCodeConstants.INVALID_DICT_DATA_IDS));
    }

    private String firstNotBlank(String preferredValue, String defaultValue) {
        return trimToNull(preferredValue) == null ? defaultValue : preferredValue;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    /**
     * 校验业务数据。
     */
    private QuestionCaptureRegion checkWaitingRegion(Long id) {
        QuestionCaptureRegion region = questionCaptureRegionService.getById(id);
        if (region == null || !Integer.valueOf(REGION_WAITING_CONFIRM).equals(region.getStatus())) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        ensureTaskWaitingConfirm(checkTask(region.getTaskId()));
        return region;
    }

    /**
     * 校验业务数据。
     */
    private QuestionCaptureTask checkTask(Long id) {
        QuestionCaptureTask task = questionCaptureTaskService.getOne(new LambdaQueryWrapper<QuestionCaptureTask>()
                .eq(QuestionCaptureTask::getId, id)
                .eq(QuestionCaptureTask::getCreateId, AccountUtils.getUserId()));
        if (task == null) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        return task;
    }

    /**
     * 执行 ensureTaskWaitingConfirm 辅助处理。
     */
    private void ensureTaskWaitingConfirm(QuestionCaptureTask task) {
        if (!Integer.valueOf(TASK_WAITING_CONFIRM).equals(task.getStatus())) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
    }

    /**
     * 校验业务数据。
     */
    private void validateRegionPosition(Integer left, Integer top, Integer width, Integer height) {
        if (left == null && top == null && width == null && height == null) {
            return;
        }
        // OCR 可能识别出公式、标题等较窄题块；只要求正尺寸，不能用人工拖拽的最小框限制拒绝原始 OCR 坐标。
        if (left == null || top == null || width == null || height == null || left < 0 || top < 0 || width <= 0 || height <= 0
                || left + width > 10000 || top + height > 10000) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
    }

    /**
     * 标准化并计算业务数据。
     */
    private int defaultValue(Integer value) {
        return defaultValue(value, 0);
    }

    /**
     * 标准化并计算业务数据。
     */
    private int defaultValue(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 执行 emptyDefault 辅助处理。
     */
    private String emptyDefault(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }

    /** 异步消费者只能在任务、页面和重试状态提交后读取这些记录。 */
    private void dispatchAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }

    /** 用于重复提示的轻量文本指纹：忽略空白和常见中英文标点，保留题目语义字符。 */
    private String questionFingerprint(String content) {
        if (content == null) {
            return "";
        }
        return content.replaceAll("[\\s\\p{Punct}，。！？；：、】【（）《》‘’“”]+", "").toLowerCase(java.util.Locale.ROOT);
    }
}
