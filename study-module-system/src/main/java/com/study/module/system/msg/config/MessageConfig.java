package com.study.module.system.msg.config;

import lombok.Data;

/**
 * 消息配置主体
 */
@Data
public class MessageConfig {

    /**
     * 消息类型文本
     */
    private String text;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;
}
