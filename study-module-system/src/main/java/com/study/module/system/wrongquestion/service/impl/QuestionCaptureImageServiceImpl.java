package com.study.module.system.wrongquestion.service.impl;

import com.study.api.contants.UploadType;
import com.study.api.provider.FileProvider;
import com.study.module.system.file.domain.LocalFileData;
import com.study.module.system.file.entity.File;
import com.study.module.system.file.service.FileService;
import com.study.module.system.file.service.LocalStorageService;
import com.study.module.system.wrongquestion.entity.QuestionCapturePage;
import com.study.module.system.wrongquestion.entity.QuestionCaptureTask;
import com.study.module.system.wrongquestion.service.QuestionCaptureImageService;
import com.study.module.system.wrongquestion.service.QuestionCaptureEventService;
import com.study.module.system.wrongquestion.service.QuestionCaptureMetricsService;
import com.study.module.system.wrongquestion.service.QuestionCapturePageService;
import com.study.module.system.wrongquestion.service.QuestionCaptureTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;

/**
 * 生成采集页面的灰度预览（不去除笔迹），失败不影响原图 OCR 及题块确认。
 */
@Service
@Slf4j
public class QuestionCaptureImageServiceImpl implements QuestionCaptureImageService {

    @Autowired
    private QuestionCapturePageService questionCapturePageService;
    @Autowired
    private QuestionCaptureTaskService questionCaptureTaskService;
    @Autowired
    private LocalStorageService localStorageService;
    @Autowired
    private FileService fileService;
    @Autowired
    private QuestionCaptureMetricsService questionCaptureMetricsService;
    @Autowired
    private QuestionCaptureEventService questionCaptureEventService;

    @DubboReference
    private FileProvider fileProvider;

    /**
     * 处理相关业务数据。
     */
    @Override
    @Async("questionCaptureExecutor")
    public void generateCleanedImage(Long pageId) {
        long startTime = System.currentTimeMillis();
        QuestionCapturePage page = questionCapturePageService.getById(pageId);
        if (page == null || page.getCleanedFileId() != null || Integer.valueOf(1).equals(page.getCleanStatus())) {
            return;
        }
        QuestionCaptureTask task = questionCaptureTaskService.getById(page.getTaskId());
        if (task == null || task.getCreateId() == null) {
            log.warn("采集页面净化图生成跳过：任务不存在或没有归属用户，pageId={}", pageId);
            return;
        }
        updateCleanStatus(page, 1, null);
        try (InputStream inputStream = new URL(fileProvider.downloadUrlByUserId(Math.toIntExact(page.getImageFileId()),
                UploadType.WRONG_QUESTION, task.getCreateId())).openStream()) {
            BufferedImage source = ImageIO.read(inputStream);
            if (source == null) {
                throw new IllegalArgumentException("无法读取图片内容");
            }
            BufferedImage cleaned = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D graphics = cleaned.createGraphics();
            graphics.drawImage(source, 0, 0, null);
            graphics.dispose();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(cleaned, "png", outputStream);
            LocalFileData localFile = localStorageService.upload(UploadType.WRONG_QUESTION, new ByteArrayInputStream(outputStream.toByteArray()), "capture-clean.png");
            File file = fileService.assembleFile(UploadType.WRONG_QUESTION, localFile, "capture-clean.png", "png",
                    outputStream.size(), task.getCreateId());
            // assembleFile 已完成文件记录入库，避免重复插入导致主键冲突。
            page.setCleanedFileId(file.getId().longValue());
            updateCleanStatus(page, 2, null);
            long elapsedMillis = System.currentTimeMillis() - startTime;
            questionCaptureMetricsService.recordCleanImageResult(true, elapsedMillis);
            questionCaptureEventService.record(task.getId(), page.getId(), null, task.getCreateId(), file.getId().longValue(),
                    "PAGE_CLEANED", "SUCCESS", "LOCAL_IMAGE", "grayscale-v1", null, elapsedMillis, null);
        } catch (Exception e) {
            log.warn("采集页面净化图生成失败，pageId={}", pageId, e);
            updateCleanStatus(page, 4, "灰度预览生成失败，请使用原图确认");
            long elapsedMillis = System.currentTimeMillis() - startTime;
            questionCaptureMetricsService.recordCleanImageResult(false, elapsedMillis);
            questionCaptureEventService.record(task.getId(), page.getId(), null, task.getCreateId(), page.getImageFileId(),
                    "PAGE_CLEANED", "FAILED", "LOCAL_IMAGE", "grayscale-v1", null, elapsedMillis, e.getMessage());
        }
    }

    /**
     * 更新业务数据。
     */
    private void updateCleanStatus(QuestionCapturePage page, Integer cleanStatus, String cleanFailReason) {
        page.setCleanStatus(cleanStatus);
        page.setCleanFailReason(cleanFailReason);
        page.setUpdateTime(LocalDateTime.now());
        questionCapturePageService.updateById(page);
    }
}
