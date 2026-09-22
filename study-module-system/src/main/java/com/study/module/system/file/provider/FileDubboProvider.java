package com.study.module.system.file.provider;

import cn.hutool.core.io.FileUtil;
import com.study.api.dto.request.FileUrlData;
import com.study.api.dto.response.FileData;
import com.study.api.dto.response.FilePreviewData;
import com.study.api.dto.response.FileSignatureData;
import com.study.api.provider.FileProvider;
import com.study.common.core.exception.LogicException;
import com.study.module.system.file.config.FileProperties;
import com.study.module.system.file.contants.FileConstants;
import com.study.module.system.file.convert.FileConvert;
import com.study.module.system.file.entity.File;
import com.study.module.system.file.service.DownloadService;
import com.study.module.system.file.service.FileService;
import com.study.module.system.file.service.PreviewFileService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件控制器(对内调用)
 */
@DubboService
public class FileDubboProvider implements FileProvider {

    @Autowired
    FileService fileService;

    @Autowired
    DownloadService downloadService;

    @Autowired
    PreviewFileService previewFileService;

    @Autowired
    FileProperties fileProperties;

    /**
     * 校验文件
     */
    @Override
    public FileData checkFile(String uploadType, Integer fileId) throws LogicException {
        // 校验文件
        File file = fileService.checkFile(fileId, uploadType);
        return FileConvert.INSTANCE.toFileData(file);
    }

    /**
     * 复制文件
     */
    @Override
    public FileData copyFile(String sourceUploadType, Integer sourceFileId, String targetUploadType) throws LogicException {
        // 复制服务器上的文件
        File file = fileService.checkFile(sourceFileId, sourceUploadType);
        String newPath = "";
        int firstSlashIndex = file.getFilePath().indexOf('/');
        if (firstSlashIndex != -1) {
            // 获取第一个目录名之后的子字符串
            newPath = targetUploadType + file.getFilePath().substring(firstSlashIndex);
        }
        String basedir = fileProperties.getBasePath() + FileConstants.SEPARATOR;
        FileUtil.copyFile(basedir + file.getFilePath(), basedir + newPath, StandardCopyOption.REPLACE_EXISTING);
        // 复制一份新的文件记录
        file.setId(null);
        file.setUploadType(targetUploadType);
        file.setFilePath(newPath);
        File copyFile = fileService.createFile(file);
        // 返回新的文件记录数据
        return FileConvert.INSTANCE.toFileData(copyFile);
    }

    /**
     * 获取文件
     */
    @Override
    public FileData getFile(String uploadType, Integer fileId) throws LogicException {
        if (Objects.isNull(fileId) || fileId == 0) {
            return null;
        }
        File file = fileService.getFile(fileId, uploadType);
        return FileConvert.INSTANCE.toFileData(file);
    }

    /**
     * 校验文件列表
     */
    @Override
    public List<FileData> checkFileList(String uploadType, String fileIds) throws LogicException {
        List<File> fileList = fileService.checkFileList(uploadType, fileIds);
        return covertFileData(fileList);
    }

    /**
     * 获取文件List
     */
    @Override
    public List<FileData> getFileList(String uploadType, String fileIds) throws LogicException {
        if (ObjectUtils.isEmpty(fileIds)) {
            return Collections.emptyList();
        }
        List<File> fileList = fileService.getFileList(uploadType, fileIds);
        return covertFileData(fileList);
    }

    /**
     * 下载凭证
     */
    @Override
    public FileSignatureData downloadSignature(Integer id, String uploadType) throws LogicException {
        return downloadService.downSignature(id, uploadType);
    }

    /**
     * 下载地址
     */
    @Override
    public String downloadUrl(Integer fileId, String uploadType) throws LogicException {
        return downloadService.downUrl(fileId, uploadType);
    }

    @Override
    public String downloadUrlByUserId(Integer fileId, String uploadType, Long userId) throws LogicException {
        return downloadService.downUrlByUserId(fileId, uploadType, userId);
    }

    /**
     * 下载地址
     */
    @Override
    public String downloadUrl(String signature) throws LogicException {
        return downloadService.downUrl(signature);
    }

    /**
     * 预览地址
     */
    @Override
    public FilePreviewData previewUrl(Integer fileId, String uploadType) throws LogicException {
        return previewFileService.previewUrl(fileId, uploadType);
    }

    /**
     * 创建Url文件
     */
    @Override
    public String createUrlFile(String uploadType, List<FileUrlData> fileUrlDataList, Long createId) throws LogicException {
        return fileService.createUrlFile(uploadType, fileUrlDataList, createId);
    }

    /**
     * 基础文件信心转换成公共文件信息列表
     */
    private List<FileData> covertFileData(List<File> fileList) throws LogicException {
        return fileList.stream().map(FileConvert.INSTANCE::toFileData).collect(Collectors.toList());
    }
}
