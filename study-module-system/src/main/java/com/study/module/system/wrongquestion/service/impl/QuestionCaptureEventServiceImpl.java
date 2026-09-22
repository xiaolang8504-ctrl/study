package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.wrongquestion.entity.QuestionCaptureEvent;
import com.study.module.system.wrongquestion.mapper.QuestionCaptureEventMapper;
import com.study.module.system.wrongquestion.service.QuestionCaptureEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 题目采集关键事件持久化实现。
 */
@Slf4j
@Service
public class QuestionCaptureEventServiceImpl extends ServiceImpl<QuestionCaptureEventMapper, QuestionCaptureEvent>
        implements QuestionCaptureEventService {

    private static final int ERROR_MESSAGE_MAX_LENGTH = 500;

    @Override
    public void record(Long taskId, Long pageId, Long regionId, Long userId, Long fileId, String eventType,
                       String result, String ocrProvider, String ocrModel, Integer regionCount,
                       Long elapsedMillis, String errorMessage) {
        try {
            QuestionCaptureEvent event = new QuestionCaptureEvent();
            event.setTaskId(taskId);
            event.setPageId(pageId);
            event.setRegionId(regionId);
            event.setUserId(userId);
            event.setFileId(fileId);
            event.setEventType(eventType);
            event.setResult(result);
            event.setOcrProvider(ocrProvider);
            event.setOcrModel(ocrModel);
            event.setRegionCount(regionCount);
            event.setElapsedMillis(elapsedMillis);
            event.setErrorMessage(abbreviate(errorMessage));
            event.setCreateTime(LocalDateTime.now());
            this.save(event);
        } catch (Exception e) {
            // 采集审计不可反向影响 OCR、建题和复习主链路。
            log.error("记录题目采集事件失败，taskId={}, eventType={}", taskId, eventType, e);
        }
    }

    private String abbreviate(String errorMessage) {
        if (errorMessage == null) {
            return null;
        }
        return errorMessage.length() <= ERROR_MESSAGE_MAX_LENGTH ? errorMessage
                : errorMessage.substring(0, ERROR_MESSAGE_MAX_LENGTH);
    }
}
