package com.study.module.system.file.service;

import java.io.InputStream;

/**
 * 文件病毒扫描服务。
 */
public interface FileVirusScanService {

    /**
     * 扫描文件内容；启用扫描后发现病毒或扫描服务异常均按配置处理。
     */
    void scan(InputStream inputStream);
}
