package com.study.api.provider;

import com.study.api.dto.request.FileUrlData;
import com.study.api.dto.response.FileData;
import com.study.api.dto.response.FilePreviewData;
import com.study.api.dto.response.FileSignatureData;
import com.study.common.core.exception.LogicException;

import java.util.List;

/**
 * 文件服务
 */
public interface FileProvider {

    /**
     * 校验文件
     */
    FileData checkFile(String uploadType, Integer fileId) throws LogicException;

    /**
     * 复制文件
     */
    FileData copyFile(String sourceUploadType, Integer sourceFileId, String targetUploadType) throws LogicException;

    /**
     * 获取文件
     */
    FileData getFile(String uploadType, Integer fileId) throws LogicException;

    /**
     * 校验文件列表
     */
    List<FileData> checkFileList(String uploadType, String fileIds) throws LogicException;

    /**
     * 获取文件List
     */
    List<FileData> getFileList(String uploadType, String fileIds) throws LogicException;

    /**
     * 下载凭证
     */
    FileSignatureData downloadSignature(Integer id, String uploadType) throws LogicException;

    /**
     * 下载地址
     */
    String downloadUrl(Integer fileId, String uploadType) throws LogicException;

    /**
     * 为指定文件所有者生成下载地址。用于没有 HTTP 安全上下文的异步任务，
     * 服务端仍会校验文件归属，不会绕过私有文件权限。
     */
    String downloadUrlByUserId(Integer fileId, String uploadType, Long userId) throws LogicException;

    /**
     * 下载地址
     */
    String downloadUrl(String signature) throws LogicException;

    /**
     * 预览地址
     */
    FilePreviewData previewUrl(Integer fileId, String uploadType) throws LogicException;

    /**
     * 创建Url文件
     */
    String createUrlFile(String uploadType, List<FileUrlData> fileUrlDataList, Long createId) throws LogicException;
}
