package com.study.module.system.wrongquestion.service.impl;

import com.study.api.contants.UploadType;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.file.config.FileProperties;
import com.study.module.system.file.domain.LocalFileData;
import com.study.module.system.file.entity.File;
import com.study.module.system.file.service.FileService;
import com.study.module.system.file.service.LocalStorageService;
import com.study.module.system.wrongquestion.entity.QuestionCapturePage;
import com.study.module.system.wrongquestion.entity.QuestionCaptureRegion;
import com.study.module.system.wrongquestion.entity.QuestionCaptureTask;
import com.study.module.system.wrongquestion.service.QuestionCaptureDocumentService;
import com.study.module.system.wrongquestion.service.QuestionCapturePageService;
import com.study.module.system.wrongquestion.service.QuestionCaptureRegionService;
import com.study.module.system.wrongquestion.service.QuestionCaptureTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 不依赖 Office 安装环境的 DOCX 轻量导入器。保留原文档作来源文件，并将可编辑文本
 * 渲染为工作台预览页；复杂公式、图片和表格始终可回看原 DOCX，确认前不可自动当作答案。
 */
@Service
@Slf4j
public class QuestionCaptureDocumentServiceImpl implements QuestionCaptureDocumentService {
    @Autowired private QuestionCaptureTaskService questionCaptureTaskService;
    @Autowired private QuestionCapturePageService questionCapturePageService;
    @Autowired private QuestionCaptureRegionService questionCaptureRegionService;
    @Autowired private FileService fileService;
    @Autowired private LocalStorageService localStorageService;
    @Autowired private FileProperties fileProperties;

    @Override
    @Async("questionCaptureExecutor")
    public void importQuestionCaptureDocuments(Long taskId, List<Long> docxFileIds, String documentContent, String documentTitle) {
        QuestionCaptureTask task = questionCaptureTaskService.getById(taskId);
        if (task == null) return;
        task.setStatus(1); task.setFailReason(null); task.setUpdateTime(LocalDateTime.now()); questionCaptureTaskService.updateById(task);
        try {
            List<DocumentSource> sources = new ArrayList<>();
            if (docxFileIds != null) for (Long fileId : docxFileIds) sources.add(readDocx(fileId, task.getCreateId()));
            if (documentContent != null && !documentContent.trim().isEmpty()) sources.add(new DocumentSource(null,
                    normalizeHtml(documentContent, documentTitle), extractHtmlImages(documentContent)));
            int pageNo = 1;
            for (DocumentSource source : sources) createEditablePage(task, source, pageNo++);
            task.setStatus(2); task.setUpdateTime(LocalDateTime.now()); questionCaptureTaskService.updateById(task);
        } catch (Exception exception) {
            log.error("DOCX/网页内容导入失败, taskId={}", taskId, exception);
            task.setStatus(4); task.setFailReason("文档解析失败，请改用网页粘贴或上传图片/PDF"); task.setUpdateTime(LocalDateTime.now()); questionCaptureTaskService.updateById(task);
        }
    }

    private void createEditablePage(QuestionCaptureTask task, DocumentSource source, int pageNo) throws Exception {
        String text = source.text == null || source.text.trim().isEmpty() ? "文档没有可提取的文字，请结合原文档手工补充题干。" : source.text.trim();
        byte[] preview = renderPreview(text, source.images);
        String fileName = "document-preview-" + task.getId() + "-" + pageNo + ".png";
        LocalFileData localFile = localStorageService.upload(UploadType.WRONG_QUESTION, new ByteArrayInputStream(preview), fileName);
        File previewFile = fileService.assembleFile(UploadType.WRONG_QUESTION, localFile, fileName, "png", preview.length, task.getCreateId());
        LocalDateTime now = LocalDateTime.now();
        QuestionCapturePage page = new QuestionCapturePage(); page.setTaskId(task.getId()); page.setImageFileId(previewFile.getId().longValue()); page.setSourceFileId(source.fileId);
        page.setSourcePageNo(1); page.setPageNo(pageNo); page.setStatus(2); page.setRetryCount(0); page.setCleanStatus(0); page.setCreateTime(now); page.setUpdateTime(now); questionCapturePageService.save(page);
        QuestionCaptureRegion region = new QuestionCaptureRegion(); region.setTaskId(task.getId()); region.setPageId(page.getId()); region.setRegionNo(1); region.setConfidence(10000); region.setQuestionTitleConfidence(10000); region.setQuestionContentConfidence(10000);
        region.setLeftPosition(0); region.setTopPosition(0); region.setWidth(10000); region.setHeight(10000); region.setStatus(0); region.setQuestionTitle("文档导入内容"); region.setQuestionContent(text); region.setGrade(task.getGrade()); region.setSubject(task.getSubject()); region.setQuestionType(task.getQuestionType()); region.setSource(task.getSource()); region.setLearningPoint(task.getLearningPoint()); region.setErrorLabels(task.getErrorLabels()); region.setCleanImageMode("ORIGINAL"); region.setCreateTime(now); region.setUpdateTime(now); questionCaptureRegionService.save(region);
    }

    private DocumentSource readDocx(Long fileId, Long userId) throws Exception {
        File source = fileService.checkUserFile(Math.toIntExact(fileId), UploadType.WRONG_QUESTION, userId);
        Path basePath = Paths.get(fileProperties.getBasePath()).toAbsolutePath().normalize(); Path path = basePath.resolve(source.getFilePath()).normalize();
        if (!path.startsWith(basePath) || !Files.isRegularFile(path)) throw new LogicException(ErrorCodeConstants.FILE_PATH_NOT_EXIST);
        try (ZipFile zip = new ZipFile(path.toFile())) {
            ZipEntry entry = zip.getEntry("word/document.xml");
            String text = entry == null ? "" : extractWordText(zip.getInputStream(entry));
            List<BufferedImage> images = new ArrayList<>();
            java.util.Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry media = entries.nextElement();
                if (!media.isDirectory() && media.getName().startsWith("word/media/")) {
                    try (InputStream imageStream = zip.getInputStream(media)) { BufferedImage image = ImageIO.read(imageStream); if (image != null) images.add(image); }
                }
            }
            return new DocumentSource(fileId, text, images);
        }
    }

    private String extractWordText(InputStream input) throws Exception {
        String xml = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        // 保留段落、表格单元格和 OMML 公式中的文本顺序；最终仍在确认页允许修正。
        return xml.replaceAll("(?i)</w:tr>", "\n").replaceAll("(?i)</w:tc>", " | ")
                .replaceAll("(?i)</w:p>", "\n").replaceAll("<[^>]+>", "")
                .replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")
                .replaceAll("[ \\t]+", " ").replaceAll("\\n[ \\t]*\\n+", "\n").trim();
    }

    private String normalizeHtml(String html, String title) {
        String text = html.replaceAll("(?is)<(script|style)[^>]*>.*?</\\1>", " ").replaceAll("(?i)<br\\s*/?>", "\n").replaceAll("(?i)</(p|div|tr|li|h[1-6])>", "\n").replaceAll("<[^>]+>", " ").replace("&nbsp;", " ").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&").replaceAll("[ \\t]+", " ").replaceAll("\\n{3,}", "\n\n").trim();
        return title == null || title.trim().isEmpty() ? text : title.trim() + "\n" + text;
    }

    private List<BufferedImage> extractHtmlImages(String html) {
        List<BufferedImage> images = new ArrayList<>(); java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?is)<img[^>]+src\\s*=\\s*['\\\"](data:image/[^'\\\"]+)['\\\"]").matcher(html);
        while (matcher.find()) try { String data = matcher.group(1); int comma = data.indexOf(','); if (comma > 0) { BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(data.substring(comma + 1)))); if (image != null) images.add(image); } } catch (Exception ignored) { }
        return images;
    }

    private byte[] renderPreview(String text, List<BufferedImage> images) throws Exception {
        List<String> lines = wrap(text, 58); int imageHeight = (images == null ? 0 : images.size() * 330); int height = Math.max(900, Math.min(9000, 140 + lines.size() * 32 + imageHeight)); BufferedImage image = new BufferedImage(1200, height, BufferedImage.TYPE_INT_RGB); Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE); graphics.fillRect(0, 0, 1200, height); graphics.setColor(new Color(42, 54, 79)); graphics.setFont(new Font("SansSerif", Font.BOLD, 26)); graphics.drawString("PC 文档导入预览（确认前可编辑）", 48, 62); graphics.setFont(new Font("SansSerif", Font.PLAIN, 22)); graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); int y = 112; for (String line : lines) { graphics.drawString(line, 48, y); y += 32; if (y > height - 32) break; }
        if (images != null) for (BufferedImage source : images) { if (y > height - 80) break; int width = Math.min(1000, source.getWidth()); int drawHeight = Math.max(1, source.getHeight() * width / Math.max(1, source.getWidth())); drawHeight = Math.min(drawHeight, 280); graphics.drawImage(source, 48, y + 16, width, drawHeight, null); y += drawHeight + 38; } graphics.dispose(); ByteArrayOutputStream output = new ByteArrayOutputStream(); ImageIO.write(image, "png", output); return output.toByteArray();
    }

    private List<String> wrap(String text, int limit) { List<String> lines = new ArrayList<>(); StringBuilder current = new StringBuilder(); for (String part : text.replace('\r', '\n').split("\\s+")) { if (current.length() + part.length() + 1 > limit) { lines.add(current.toString()); current.setLength(0); } if (current.length() > 0) current.append(' '); current.append(part); } if (current.length() > 0) lines.add(current.toString()); return lines.isEmpty() ? java.util.Collections.singletonList("（空文档）") : lines; }
    private static class DocumentSource { private final Long fileId; private final String text; private final List<BufferedImage> images; private DocumentSource(Long fileId, String text, List<BufferedImage> images) { this.fileId = fileId; this.text = text; this.images = images; } }
}
