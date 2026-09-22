package com.study.module.system.msg.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

/**
 * 消息配置映射
 */
@Configuration
@ConfigurationProperties(prefix = "msg")
@Data
public class MsgConfig {

    /**
     * 消息类型配置
     */
    private Map<String, MessageConfig> msgType;
}
