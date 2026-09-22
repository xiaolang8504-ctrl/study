package com.study.module.system.wrongquestion.config;

import com.study.api.contants.UploadType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 错题OCR配置
 */
@Configuration
@ConfigurationProperties(prefix = "wrong-question.ocr.paddle")
@Data
public class WrongQuestionOcrConfig {

    /**
     * 是否启用飞桨OCR
     */
    private Boolean enabled = Boolean.TRUE;

    /**
     * 任务提交地址
     */
    private String jobUrl = "https://paddleocr.aistudio-app.com/api/v2/ocr/jobs";

    /**
     * 访问令牌
     */
    private String token;

    /**
     * 模型名称
     */
    private String model = "PaddleOCR-VL-1.6";

    /**
     * 文件服务上传类型
     */
    private String uploadType = UploadType.WRONG_QUESTION;

    /**
     * 是否启用文档方向分类
     */
    private Boolean useDocOrientationClassify = Boolean.FALSE;

    /**
     * 是否启用文档矫正
     */
    private Boolean useDocUnwarping = Boolean.FALSE;

    /**
     * 是否启用图表识别
     */
    private Boolean useChartRecognition = Boolean.FALSE;

    /**
     * 连接超时时间
     */
    private Integer connectTimeout = 10000;

    /**
     * 读取超时时间
     */
    private Integer readTimeout = 60000;

    /**
     * 轮询间隔，毫秒
     */
    private Integer pollInterval = 5000;

    /**
     * 最大轮询次数
     */
    private Integer maxPollTimes = 60;
}
