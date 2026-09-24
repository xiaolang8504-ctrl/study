package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.study.api.contants.UploadType;
import com.study.api.provider.FileProvider;
import com.study.module.system.file.domain.LocalFileData;
import com.study.module.system.file.entity.File;
import com.study.module.system.file.service.FileService;
import com.study.module.system.file.service.LocalStorageService;
import com.study.module.system.wrongquestion.domain.QuestionCaptureCleanResult;
import com.study.module.system.wrongquestion.entity.QuestionCaptureCleanVersion;
import com.study.module.system.wrongquestion.entity.QuestionCapturePage;
import com.study.module.system.wrongquestion.entity.QuestionCaptureTask;
import com.study.module.system.wrongquestion.mapper.QuestionCaptureCleanVersionMapper;
import com.study.module.system.wrongquestion.service.QuestionCaptureEventService;
import com.study.module.system.wrongquestion.service.QuestionCaptureCleanProvider;
import com.study.module.system.wrongquestion.service.QuestionCaptureImageService;
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
 * 本地页面处理实现。灰度图只承担预览；真正的清理仅安全去除红、蓝等彩色笔迹，保留印刷黑字。
 * 没有足够的可安全去除笔迹时会失败并继续使用原图，不把灰度图伪装成清理结果。
 */
@Service
@Slf4j
public class QuestionCaptureImageServiceImpl implements QuestionCaptureImageService {

    private static final String GRAYSCALE_ALGORITHM_VERSION = "grayscale-preview-v1";
    private static final double MIN_REMOVED_RATIO = 0.0001D;

    @Autowired private QuestionCapturePageService questionCapturePageService;
    @Autowired private QuestionCaptureTaskService questionCaptureTaskService;
    @Autowired private LocalStorageService localStorageService;
    @Autowired private FileService fileService;
    @Autowired private QuestionCaptureMetricsService questionCaptureMetricsService;
    @Autowired private QuestionCaptureEventService questionCaptureEventService;
    @Autowired private QuestionCaptureCleanVersionMapper questionCaptureCleanVersionMapper;
    @Autowired private QuestionCaptureCleanProvider questionCaptureCleanProvider;
    @DubboReference private FileProvider fileProvider;

    @Override
    @Async("questionCaptureExecutor")
    public void generateCleanedImage(Long pageId) {
        long startTime = System.currentTimeMillis();
        QuestionCapturePage page = questionCapturePageService.getById(pageId);
        if (page == null || page.getImageFileId() == null || Integer.valueOf(1).equals(page.getCleanStatus())
                || (Integer.valueOf(2).equals(page.getCleanStatus()) && page.getCleanedFileId() != null)) return;
        QuestionCaptureTask task = questionCaptureTaskService.getById(page.getTaskId());
        if (task == null || task.getCreateId() == null) {
            log.warn("采集页面清理跳过：任务不存在或没有归属用户，pageId={}", pageId);
            return;
        }
        LocalDateTime requestedTime = LocalDateTime.now();
        page.setCleanStatus(1); page.setCleanFailReason(null); page.setCleanProvider(questionCaptureCleanProvider.providerCode());
        page.setCleanAlgorithmVersion(questionCaptureCleanProvider.algorithmVersion()); page.setCleanRequestedTime(requestedTime);
        page.setCleanFinishedTime(null); page.setCleanQualityScore(null); page.setUpdateTime(requestedTime);
        questionCapturePageService.updateById(page);
        try (InputStream inputStream = new URL(fileProvider.downloadUrlByUserId(Math.toIntExact(page.getImageFileId()),
                UploadType.WRONG_QUESTION, task.getCreateId())).openStream()) {
            BufferedImage source = ImageIO.read(inputStream);
            if (source == null) throw new IllegalArgumentException("无法读取图片内容");
            generateGrayscalePreviewIfNeeded(page, task, source);
            QuestionCaptureCleanResult cleaned = questionCaptureCleanProvider.clean(source);
            if (cleaned.getRemovedRatio() < MIN_REMOVED_RATIO) {
                throw new InsufficientCleanResultException("未识别到可安全清除的彩色笔迹，已保留原图");
            }
            File cleanedFile = saveImage(task, cleaned.getImage(), "capture-handwriting-clean.png");
            LocalDateTime finishedTime = LocalDateTime.now();
            page.setCleanedFileId(cleanedFile.getId().longValue()); page.setCleanStatus(2); page.setCleanFailReason(null);
            page.setCleanQualityScore((int) Math.round(cleaned.getRemovedRatio() * 10000D));
            page.setCleanFinishedTime(finishedTime); page.setUpdateTime(finishedTime);
            questionCapturePageService.updateById(page);
            saveCleanVersion(page, task, 2, null, 1);
            long elapsedMillis = System.currentTimeMillis() - startTime;
            questionCaptureMetricsService.recordCleanImageResult(true, elapsedMillis);
            questionCaptureEventService.record(task.getId(), page.getId(), null, task.getCreateId(),
                    cleanedFile.getId().longValue(), "PAGE_HANDWRITING_CLEANED", "SUCCESS", page.getCleanProvider(),
                    page.getCleanAlgorithmVersion(), null, elapsedMillis, null);
        } catch (Exception exception) {
            log.warn("采集页面去笔迹清理失败，pageId={}", pageId, exception);
            LocalDateTime finishedTime = LocalDateTime.now();
            page.setCleanedFileId(null); page.setCleanStatus(4); page.setCleanFailReason(safeFailureReason(exception));
            page.setCleanFinishedTime(finishedTime); page.setUpdateTime(finishedTime);
            questionCapturePageService.updateById(page);
            saveCleanVersion(page, task, 4, page.getCleanFailReason(), 0);
            long elapsedMillis = System.currentTimeMillis() - startTime;
            questionCaptureMetricsService.recordCleanImageResult(false, elapsedMillis);
            questionCaptureEventService.record(task.getId(), page.getId(), null, task.getCreateId(), page.getImageFileId(),
                    "PAGE_HANDWRITING_CLEANED", "FAILED", page.getCleanProvider(), page.getCleanAlgorithmVersion(), null,
                    elapsedMillis, exception.getMessage());
        }
    }

    @Override
    public void revertCleanedImage(Long pageId) {
        QuestionCapturePage page = questionCapturePageService.getById(pageId);
        if (page == null) return;
        QuestionCaptureTask task = questionCaptureTaskService.getById(page.getTaskId());
        if (task == null || task.getCreateId() == null) return;
        LocalDateTime now = LocalDateTime.now();
        questionCaptureCleanVersionMapper.update(null, new LambdaUpdateWrapper<QuestionCaptureCleanVersion>()
                .eq(QuestionCaptureCleanVersion::getPageId, pageId).eq(QuestionCaptureCleanVersion::getActiveFlag, 1)
                .set(QuestionCaptureCleanVersion::getActiveFlag, 0).set(QuestionCaptureCleanVersion::getUpdateTime, now));
        page.setCleanedFileId(null); page.setCleanStatus(0); page.setCleanFailReason(null); page.setCleanProvider(null);
        page.setCleanAlgorithmVersion(null); page.setCleanQualityScore(null); page.setCleanRequestedTime(null);
        page.setCleanFinishedTime(null); page.setUpdateTime(now);
        questionCapturePageService.updateById(page);
        questionCaptureEventService.record(task.getId(), pageId, null, task.getCreateId(), page.getImageFileId(),
                "PAGE_CLEAN_REVERTED", "SUCCESS", questionCaptureCleanProvider.providerCode(),
                questionCaptureCleanProvider.algorithmVersion(), null, null, null);
    }

    @Override
    public void applyManualCleanImage(Long pageId, Long cleanedFileId) {
        QuestionCapturePage page = questionCapturePageService.getById(pageId);
        if (page == null || page.getImageFileId() == null) {
            throw new IllegalArgumentException("采集页面不存在或没有原图");
        }
        QuestionCaptureTask task = questionCaptureTaskService.getById(page.getTaskId());
        if (task == null || task.getCreateId() == null) {
            throw new IllegalArgumentException("采集任务不存在或没有归属用户");
        }
        File cleanFile = fileService.checkUserFile(Math.toIntExact(cleanedFileId), UploadType.WRONG_QUESTION, task.getCreateId());
        String extension = cleanFile.getFileExtension();
        if (extension == null || !("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension)
                || "png".equalsIgnoreCase(extension) || "webp".equalsIgnoreCase(extension) || "bmp".equalsIgnoreCase(extension))) {
            throw new IllegalArgumentException("人工遮罩图必须是图片文件");
        }
        validateManualImageDimensions(page, task, cleanedFileId);
        LocalDateTime now = LocalDateTime.now();
        page.setCleanedFileId(cleanedFileId);
        page.setCleanStatus(2);
        page.setCleanFailReason(null);
        page.setCleanProvider("MANUAL_REDACTION");
        page.setCleanAlgorithmVersion("manual-mask-v1");
        // 人工遮罩不伪造自动算法的质量分；版本与操作者事件已经完整留痕。
        page.setCleanQualityScore(null);
        page.setCleanRequestedTime(now);
        page.setCleanFinishedTime(now);
        page.setUpdateTime(now);
        questionCapturePageService.updateById(page);
        saveCleanVersion(page, task, 2, null, 1);
        questionCaptureEventService.record(task.getId(), pageId, null, task.getCreateId(), cleanedFileId,
                "PAGE_MANUAL_MASK_APPLIED", "SUCCESS", page.getCleanProvider(), page.getCleanAlgorithmVersion(),
                null, null, null);
    }

    private void generateGrayscalePreviewIfNeeded(QuestionCapturePage page, QuestionCaptureTask task, BufferedImage source)
            throws Exception {
        if (Integer.valueOf(2).equals(page.getGrayscaleStatus()) && page.getGrayscaleFileId() != null) return;
        page.setGrayscaleStatus(1); page.setGrayscaleFailReason(null); page.setUpdateTime(LocalDateTime.now());
        questionCapturePageService.updateById(page);
        try {
            BufferedImage grayscale = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D graphics = grayscale.createGraphics(); graphics.drawImage(source, 0, 0, null); graphics.dispose();
            File previewFile = saveImage(task, grayscale, "capture-grayscale-preview.png");
            page.setGrayscaleFileId(previewFile.getId().longValue()); page.setGrayscaleStatus(2); page.setGrayscaleFailReason(null);
            page.setUpdateTime(LocalDateTime.now()); questionCapturePageService.updateById(page);
            questionCaptureEventService.record(task.getId(), page.getId(), null, task.getCreateId(),
                    previewFile.getId().longValue(), "PAGE_GRAYSCALE_PREVIEW", "SUCCESS", "LOCAL_IMAGE",
                    GRAYSCALE_ALGORITHM_VERSION, null, null, null);
        } catch (Exception exception) {
            page.setGrayscaleStatus(4); page.setGrayscaleFailReason("灰度预览生成失败，请使用原图确认");
            page.setUpdateTime(LocalDateTime.now()); questionCapturePageService.updateById(page);
            questionCaptureEventService.record(task.getId(), page.getId(), null, task.getCreateId(), page.getImageFileId(),
                    "PAGE_GRAYSCALE_PREVIEW", "FAILED", "LOCAL_IMAGE", GRAYSCALE_ALGORITHM_VERSION,
                    null, null, exception.getMessage());
        }
    }

    /**
     * 人工遮罩图必须与原始整页图尺寸一致，避免错把其他页面的图片作为当前页的清理结果。
     */
    private void validateManualImageDimensions(QuestionCapturePage page, QuestionCaptureTask task, Long cleanedFileId) {
        try (InputStream sourceInput = new URL(fileProvider.downloadUrlByUserId(Math.toIntExact(page.getImageFileId()),
                UploadType.WRONG_QUESTION, task.getCreateId())).openStream();
             InputStream cleanedInput = new URL(fileProvider.downloadUrlByUserId(Math.toIntExact(cleanedFileId),
                     UploadType.WRONG_QUESTION, task.getCreateId())).openStream()) {
            BufferedImage source = ImageIO.read(sourceInput);
            BufferedImage cleaned = ImageIO.read(cleanedInput);
            if (source == null || cleaned == null || source.getWidth() != cleaned.getWidth()
                    || source.getHeight() != cleaned.getHeight()) {
                throw new IllegalArgumentException("人工遮罩图必须与当前原图的尺寸完全一致");
            }
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("无法校验人工遮罩图，请重新上传与原图同尺寸的图片");
        }
    }

    private File saveImage(QuestionCaptureTask task, BufferedImage image, String fileName) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        LocalFileData localFile = localStorageService.upload(UploadType.WRONG_QUESTION,
                new ByteArrayInputStream(outputStream.toByteArray()), fileName);
        return fileService.assembleFile(UploadType.WRONG_QUESTION, localFile, fileName, "png", outputStream.size(),
                task.getCreateId());
    }

    private void saveCleanVersion(QuestionCapturePage page, QuestionCaptureTask task, int status, String failReason, int activeFlag) {
        LocalDateTime now = LocalDateTime.now();
        if (activeFlag == 1) questionCaptureCleanVersionMapper.update(null, new LambdaUpdateWrapper<QuestionCaptureCleanVersion>()
                .eq(QuestionCaptureCleanVersion::getPageId, page.getId()).eq(QuestionCaptureCleanVersion::getActiveFlag, 1)
                .set(QuestionCaptureCleanVersion::getActiveFlag, 0).set(QuestionCaptureCleanVersion::getUpdateTime, now));
        QuestionCaptureCleanVersion version = new QuestionCaptureCleanVersion();
        version.setPageId(page.getId()); version.setTaskId(task.getId()); version.setUserId(task.getCreateId());
        version.setOriginalFileId(page.getImageFileId()); version.setCleanedFileId(page.getCleanedFileId());
        version.setProvider(page.getCleanProvider() == null ? questionCaptureCleanProvider.providerCode() : page.getCleanProvider());
        version.setAlgorithmVersion(page.getCleanAlgorithmVersion() == null
                ? questionCaptureCleanProvider.algorithmVersion() : page.getCleanAlgorithmVersion());
        version.setStatus(status);
        version.setQualityScore(page.getCleanQualityScore()); version.setFailReason(failReason); version.setActiveFlag(activeFlag);
        version.setCreateTime(now); version.setUpdateTime(now); questionCaptureCleanVersionMapper.insert(version);
    }

    private String safeFailureReason(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) return "卷面清理失败，请使用原图确认";
        return message.length() > 500 ? message.substring(0, 500) : message;
    }

    private static class InsufficientCleanResultException extends RuntimeException {
        private InsufficientCleanResultException(String message) { super(message); }
    }
}
