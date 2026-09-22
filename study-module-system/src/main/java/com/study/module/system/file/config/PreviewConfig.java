package com.study.module.system.file.config;

import lombok.Data;

/**
 * 预览配置
 */
@Data
public class PreviewConfig {

    /**
     * 下载二进制流地址
     */
    private String downloadUrl;

    /**
     * 预览过期时间
     */
    private String expire;

    /**
     * 预览Token
     */
    private String accessToken;

    /**
     * 预览服务地址
     */
    private String previewUrl;
}
