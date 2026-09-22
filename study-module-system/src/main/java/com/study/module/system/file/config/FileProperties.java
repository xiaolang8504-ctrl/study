package com.study.module.system.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

/**
 * 文件配置
 */
@Data
@ConfigurationProperties(prefix = "file")
@Configuration
public class FileProperties {

    /**
     * 基础路径
     */
    private String basePath;

    /**
     * 文件上传配置
     */
    private Map<String, UploadConfig> upload;

    /**
     * 文件预览
     */
    private PreviewConfig preview;

    /**
     * 文件安全扫描配置。
     */
    private FileSecurityConfig security = new FileSecurityConfig();
}
