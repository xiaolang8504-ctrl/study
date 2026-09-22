package com.study.module.system.review.service.impl;
import com.study.module.system.file.domain.LocalFileData;
import com.study.module.system.file.entity.File;
import com.study.module.system.file.service.FileService;
import com.study.module.system.file.service.LocalStorageService;
import com.study.module.system.review.dto.response.PracticeSessionDetailResp;
import com.study.module.system.review.entity.PracticePaperExportTask;
import com.study.module.system.review.service.PracticePaperDocumentService;
import com.study.module.system.review.service.PracticePaperExportAsyncService;
import com.study.module.system.review.service.PracticePaperExportTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream; import java.time.LocalDateTime;
@Service public class PracticePaperExportAsyncServiceImpl implements PracticePaperExportAsyncService {
    private static final String UPLOAD_TYPE = "PRACTICE_EXPORT";
    @Autowired private PracticePaperExportTaskService taskService; @Autowired private PracticePaperDocumentService documentService; @Autowired private LocalStorageService localStorageService; @Autowired private FileService fileService;
    @Override @Async("questionCaptureExecutor") public void generatePracticePaper(Long taskId, Long userId, PracticeSessionDetailResp paper, String format, Integer answerMode) {
        PracticePaperExportTask task = taskService.getById(taskId); if (task == null) return; task.setStatus(1); task.setUpdateTime(LocalDateTime.now()); taskService.updateById(task);
        try { boolean withAnswers = Integer.valueOf(1).equals(answerMode); byte[] bytes = "PDF".equals(format) ? documentService.renderPdf(paper, withAnswers) : documentService.renderDocx(paper, withAnswers); String fileName = sanitize(paper.getTitle()) + (withAnswers ? "-答案版" : "-题目版") + "." + format.toLowerCase(); LocalFileData localFile = localStorageService.upload(UPLOAD_TYPE, new ByteArrayInputStream(bytes), fileName); File file = fileService.assembleFile(UPLOAD_TYPE, localFile, fileName, format.toLowerCase(), bytes.length, userId); task.setStatus(2); task.setFileId(file.getId()); task.setFileName(fileName); task.setFinishTime(LocalDateTime.now()); task.setUpdateTime(LocalDateTime.now()); taskService.updateById(task); } catch (Exception exception) { task.setStatus(3); task.setErrorMessage(abbreviate(exception.getMessage())); task.setFinishTime(LocalDateTime.now()); task.setUpdateTime(LocalDateTime.now()); taskService.updateById(task); }
    }
    private String sanitize(String name) { return name == null || name.trim().isEmpty() ? "专项练习" : name.replaceAll("[\\\\/:*?\"<>|]", "_"); }
    private String abbreviate(String text) { return text == null ? "生成失败" : text.substring(0, Math.min(text.length(), 500)); }
}
