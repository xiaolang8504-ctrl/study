package com.study.module.system.wrongquestion.service;

import com.study.module.system.wrongquestion.domain.WrongQuestionOcrData;

import java.util.List;

/**
 * A4错题图片识别服务
 */
public interface WrongQuestionImageOcrService {

    /**
     * 识别A4图片中的错题
     */
    List<WrongQuestionOcrData> recognize(Long imageFileId, Long userId);

    /**
     * 识别图片版面；userId 是文件所属用户，供无 HTTP 登录上下文的异步任务安全取图。
     */
    List<WrongQuestionOcrData> recognizeLayout(Long imageFileId, Long userId);
}
