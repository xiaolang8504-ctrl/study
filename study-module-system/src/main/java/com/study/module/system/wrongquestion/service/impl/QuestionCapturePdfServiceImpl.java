package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.api.contants.UploadType;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.file.config.FileProperties;
import com.study.module.system.file.domain.LocalFileData;
import com.study.module.system.file.entity.File;
import com.study.module.system.file.service.FileService;
import com.study.module.system.file.service.LocalStorageService;
import com.study.module.system.wrongquestion.entity.QuestionCapturePage;
import com.study.module.system.wrongquestion.entity.QuestionCaptureTask;
import com.study.module.system.wrongquestion.service.QuestionCaptureOcrService;
import com.study.module.system.wrongquestion.service.QuestionCapturePageService;
import com.study.module.system.wrongquestion.service.QuestionCapturePdfService;
import com.study.module.system.wrongquestion.service.QuestionCaptureTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 将 PDF 顺序渲染为采集页面，全部落库后再统一触发 OCR。
 */
@Service
@Slf4j
public class QuestionCapturePdfServiceImpl implements QuestionCapturePdfService {

    private static final int PDF_RENDER_DPI = 150;

    @Autowired
    private QuestionCapturePageService questionCapturePageService;
    @Autowired
    private QuestionCaptureTaskService questionCaptureTaskService;
    @Autowired
    private LocalStorageService localStorageService;
    @Autowired
    private FileService fileService;
    @Autowired
    private QuestionCaptureOcrService questionCaptureOcrService;
    @Autowired
    private FileProperties fileProperties;

    /**
     * 处理题目采集。
     */
    @Override
    @Async("questionCaptureExecutor")
    public void renderQuestionCapturePdfs(Long taskId, List<Long> fileIds) {
        QuestionCaptureTask processingTask = new QuestionCaptureTask();
        processingTask.setId(taskId);
        processingTask.setStatus(1);
        processingTask.setFailReason(null);
        processingTask.setUpdateTime(LocalDateTime.now());
        questionCaptureTaskService.updateById(processingTask);
        int pageNo = questionCapturePageService.list(new LambdaQueryWrapper<QuestionCapturePage>()
                        .eq(QuestionCapturePage::getTaskId, taskId))
                .stream().map(QuestionCapturePage::getPageNo).filter(item -> item != null).max(Integer::compareTo).orElse(0) + 1;
        try {
            for (Long fileId : fileIds) {
                try {
                    pageNo = renderPdf(taskId, fileId, pageNo);
                } catch (Exception e) {
                    log.error("PDF分页失败，taskId={}, sourceFileId={}", taskId, fileId, e);
                    createFailedPdfPage(taskId, fileId, pageNo++);
                }
            }
            questionCaptureOcrService.recognizeQuestionCaptureTask(taskId);
        } catch (Exception e) {
            log.error("PDF分页失败，taskId={}", taskId, e);
            QuestionCaptureTask task = new QuestionCaptureTask();
            task.setId(taskId);
            task.setStatus(4);
            task.setFailReason("PDF分页失败，请检查文件后重试");
            task.setUpdateTime(LocalDateTime.now());
            questionCaptureTaskService.updateById(task);
        }
    }

    /**
     * 处理业务数据。
     */
    private int renderPdf(Long taskId, Long sourceFileId, int pageNo) throws Exception {
        QuestionCaptureTask task = questionCaptureTaskService.getById(taskId);
        if (task == null) {
            throw new LogicException(ErrorCodeConstants.WRONG_QUESTION_IMPORT_FAIL);
        }
        try (InputStream inputStream = openSourcePdf(sourceFileId, task.getCreateId());
             PDDocument document = PDDocument.load(inputStream)) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int sourcePageNo = 0; sourcePageNo < document.getNumberOfPages(); sourcePageNo++) {
                BufferedImage image = renderer.renderImageWithDPI(sourcePageNo, PDF_RENDER_DPI);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageIO.write(image, "png", outputStream);
                LocalFileData localFile = localStorageService.upload(UploadType.WRONG_QUESTION, new ByteArrayInputStream(outputStream.toByteArray()), "capture-page-" + pageNo + ".png");
                File file = fileService.assembleFile(UploadType.WRONG_QUESTION, localFile,
                        "capture-page-" + pageNo + ".png", "png", outputStream.size(), task.getCreateId());
                // assembleFile 已完成文件记录入库，此处不可重复 save，否则会以同一主键再次插入。
                QuestionCapturePage page = new QuestionCapturePage();
                page.setTaskId(taskId);
                page.setImageFileId(file.getId().longValue());
                page.setSourceFileId(sourceFileId);
                page.setSourcePageNo(sourcePageNo + 1);
                page.setPageNo(pageNo++);
                page.setStatus(0);
                page.setRetryCount(0);
                page.setCleanStatus(0);
                page.setCreateTime(LocalDateTime.now());
                page.setUpdateTime(LocalDateTime.now());
                questionCapturePageService.save(page);
            }
        }
        return pageNo;
    }

    /**
     * PDF 分页与文件存储位于同一模块，直接读取受控本地文件，避免经由本机 HTTP 下载地址再次转发 PDF 流。
     */
    private InputStream openSourcePdf(Long sourceFileId, Long ownerUserId) throws Exception {
        File sourceFile = fileService.checkUserFile(Math.toIntExact(sourceFileId), UploadType.WRONG_QUESTION, ownerUserId);
        Path basePath = Paths.get(fileProperties.getBasePath()).toAbsolutePath().normalize();
        Path filePath = basePath.resolve(sourceFile.getFilePath()).normalize();
        if (!filePath.startsWith(basePath) || !Files.isRegularFile(filePath)) {
            throw new LogicException(ErrorCodeConstants.FILE_PATH_NOT_EXIST);
        }
        return Files.newInputStream(filePath);
    }

    /**
     * 以失败页面记录保留来源文件和错误状态，其他 PDF 页面仍可继续 OCR。
     */
    private void createFailedPdfPage(Long taskId, Long sourceFileId, int pageNo) {
        QuestionCapturePage page = new QuestionCapturePage();
        page.setTaskId(taskId);
        // 页面表 imageFileId 非空；该失败记录不会进入 OCR，保留原 PDF ID 仅用于定位来源。
        page.setImageFileId(sourceFileId);
        page.setSourceFileId(sourceFileId);
        page.setPageNo(pageNo);
        page.setStatus(4);
        page.setFailReason("PDF分页失败，请重新上传或检查该文件");
        page.setRetryCount(0);
        page.setCleanStatus(0);
        page.setCreateTime(LocalDateTime.now());
        page.setUpdateTime(LocalDateTime.now());
        questionCapturePageService.save(page);
    }
}
