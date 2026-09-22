package com.study.module.system.file.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.api.dto.request.FileUrlData;
import com.study.module.system.file.domain.LocalFileData;
import com.study.module.system.file.entity.File;

import java.util.List;

/**
 * 文件服务
 */
public interface FileService extends IService<File> {

    /**
     * 创建文件
     */
    File createFile(File file);

    /**
     * 校验文件
     */
    File checkFile(Integer id, String uploadType);

    /**
     * 获取文件
     */
    File getFile(Integer id, String uploadType);

    /**
     * 校验当前登录用户是否有权访问文件。
     */
    File checkCurrentUserFile(Integer id, String uploadType);

    /**
     * 校验指定用户是否有权访问文件，供同一服务内需要显式传递业务归属用户的流程使用。
     */
    File checkUserFile(Integer id, String uploadType, Long userId);

    /**
     * 校验文件List
     */
    List<File> checkFileList(String uploadType, String fileIds);

    /**
     * 获取文件List
     */
    List<File> getFileList(String uploadType, String fileIds);

    /**
     * 组装文件入库
     */
    File assembleFile(String uploadType, LocalFileData localFileData, String originalName,
                      String extName, long size, Long createId);

    /**
     * 创建文件
     */
    String createUrlFile(String uploadType, List<FileUrlData> fileUrlDataList, Long createId);
}
