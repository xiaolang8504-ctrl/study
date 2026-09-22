package com.study.module.system.file.service;

import com.study.api.dto.response.FileSignatureData;
import com.study.module.system.file.domain.LocalFileData;
import com.study.module.system.file.domain.UrlFileData;

import java.io.InputStream;
import java.util.Map;

/**
 * 本地文件存储
 */
public interface LocalStorageService {

    /**
     * 创建bucket
     */
    String createBucket(String bucketName);

    /**
     * 上传文件
     */
    LocalFileData upload(String bucketName, InputStream fileInputStream, String fileName);

    /**
     * 下载凭证
     */
    FileSignatureData downSignature(String originName, String filePath, String expires);

    /**
     * 生成文件
     */
    UrlFileData generateUrlFile(String uploadType, String fileUrl, String fileName);

    /**
     * 生成文件
     */
    UrlFileData generateUrlFile(String uploadType, String fileUrl, String fileName, Map<String, String> requestHeaders);

    /**
     * 删除本地存储文件
     */
    void delete(String filePath);
}
