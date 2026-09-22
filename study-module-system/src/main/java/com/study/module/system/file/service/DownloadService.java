package com.study.module.system.file.service;

import com.study.api.dto.response.FileSignatureData;

import javax.servlet.http.HttpServletResponse;

/**
 * 下载服务
 */
public interface DownloadService {

    /**
     * 下载文件
     */
    void download(String signature, HttpServletResponse response);

    /**
     * 下载凭证
     */
    FileSignatureData downSignature(Integer fileId, String uploadType);

    /**
     * 下载地址
     */
    String downUrl(Integer fileId, String uploadType);

    /**
     * 为指定用户已拥有的文件生成短期下载地址。
     */
    String downUrlByUserId(Integer fileId, String uploadType, Long userId);

    /**
     * 下载地址
     */
    String downUrl(String signature);
}
