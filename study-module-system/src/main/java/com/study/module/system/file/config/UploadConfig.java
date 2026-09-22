package com.study.module.system.file.config;

import lombok.Data;

/**
 * 上传文件配置信息
 */
@Data
public class UploadConfig {

    /**
     * 过期时间
     */
    private String expire;

    /**
     * 最大文件大小
     */
    private String maxSize;

    /**
     * 允许扩展名
     */
    private String allowedExtension;
}
