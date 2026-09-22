package com.study.module.system.log.config;

import lombok.Data;

/**
 * 日志配置主体
 */
@Data
public class LogTextConfig {

    /**
     * 消息类型文本
     */
    private String typeText;

    /**
     * 模块
     */
    private String moduleText;

    /**
     * 日志内容
     */
    private String content;
}
