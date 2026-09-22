package com.study.module.system.file.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.api.dto.request.FileUrlData;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.common.core.utils.CollUtils;
import com.study.module.system.file.config.FileProperties;
import com.study.module.system.file.contants.FileConstants;
import com.study.module.system.file.convert.FileConvert;
import com.study.module.system.file.domain.LocalFileData;
import com.study.module.system.file.domain.UrlFileData;
import com.study.module.system.file.entity.File;
import com.study.module.system.file.mapper.FileMapper;
import com.study.module.system.file.service.FileService;
import com.study.module.system.file.service.FileContentValidationService;
import com.study.module.system.file.service.FileVirusScanService;
import com.study.module.system.file.service.LocalStorageService;
import com.study.module.system.role.service.RoleService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;

/**
 * 文件服务
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    @Autowired
    LocalStorageService localStorageService;

    @Autowired
    FileProperties fileProperties;

    @Autowired
    FileContentValidationService fileContentValidationService;

    @Autowired
    FileVirusScanService fileVirusScanService;

    @Autowired
    RoleService roleService;

    /**
     * 创建文件
     */
    @Override
    public File createFile(File file) {
        // 文件业务参数校验
        validateFile(file);
        // 文件数据入库
        if (!this.save(file)) {
            throw new LogicException(ErrorCodeConstants.CREATE_FILE_FAIL);
        }
        // 返回文件信息
        return file;
    }

    /**
     * 校验文件
     */
    @Override
    public File checkFile(Integer id, String uploadType) {
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getId, id);
        queryWrapper.eq(File::getUploadType, uploadType);
        queryWrapper.last("LIMIT 1");
        File file = this.getOne(queryWrapper);
        if (null == file || ObjectUtil.isEmpty(file.getFilePath())) {
            throw new LogicException(ErrorCodeConstants.FILE_NOT_EXIST);
        }
        return file;
    }

    /**
     * 获取文件
     */
    @Override
    public File getFile(Integer id, String uploadType) {
        if (Objects.nonNull(id) && ObjectUtils.isNotEmpty(uploadType)) {
            LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(File::getId, id);
            queryWrapper.eq(File::getUploadType, uploadType);
            queryWrapper.last("LIMIT 1");
            return this.getOne(queryWrapper);
        }
        return new File();
    }

    /**
     * 校验当前登录用户拥有该文件，系统管理员可访问文件管理范围内的文件。
     */
    @Override
    public File checkCurrentUserFile(Integer id, String uploadType) {
        return checkUserFile(id, uploadType, AccountUtils.getUserId());
    }

    /**
     * 校验指定用户拥有该文件，系统管理员可访问文件管理范围内的文件。
     */
    @Override
    public File checkUserFile(Integer id, String uploadType, Long userId) {
        File file = checkFile(id, uploadType);
        if (userId == null) {
            throw new LogicException(ErrorCodeConstants.NO_LOGIN);
        }
        if (!userId.equals(file.getCreateId()) && !roleService.isAdminByUserId(userId)) {
            throw new LogicException(ErrorCodeConstants.ACCESS_DENIED);
        }
        return file;
    }

    /**
     * 校验文件List
     */
    @Override
    public List<File> checkFileList(String uploadType, String fileIds) {
        if (StringUtils.isEmpty(fileIds)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getUploadType, uploadType).in(File::getId, CollUtils.idsToList(fileIds));
        List<File> fileList = this.list(queryWrapper);

        if (fileList.size() != fileIds.split(FileConstants.FILE_SEPARATOR).length) {
            throw new LogicException(ErrorCodeConstants.INVALID_FILE_IDS);
        }
        return fileList;
    }

    /**
     * 获取文件List
     */
    @Override
    public List<File> getFileList(String uploadType, String fileIds) {
        if (!StringUtils.isEmpty(fileIds)) {
            return this.list(
                    new LambdaQueryWrapper<File>().eq(ObjectUtil.isNotEmpty(uploadType), File::getUploadType, uploadType)
                            .in(File::getId, CollUtils.idsToList(fileIds))
            );
        }
        return Collections.emptyList();
    }

    /**
     * 组装文件入库
     */
    @Override
    public File assembleFile(String uploadType, LocalFileData localFileData, String originalName,
                             String extName, long size, Long createId) {
        File file = new File();
        // 存储方式
        file.setUploadType(uploadType);
        file.setCreateId(createId);
        file.setOriginName(originalName);
        // 获取保存之后的文件名
        file.setSaveName(localFileData.getSaveFileName());
        file.setFilePath(localFileData.getFilePath());
        file.setFileExtension(extName);
        file.setFileBytes(Math.toIntExact(size));
        file.setFileSize(FileUtil.readableFileSize(size));
        file.setCreateTime(LocalDateTime.now());
        return this.createFile(file);
    }

    /**
     * 创建文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createUrlFile(String uploadType, List<FileUrlData> fileUrlDataList, Long createId) {
        List<File> fileList = new ArrayList<>();
        List<String> storedPaths = new ArrayList<>();
        try {
            for (FileUrlData fileUrlData : fileUrlDataList) {
                UrlFileData urlFileData = localStorageService.generateUrlFile(uploadType, fileUrlData.getFileUrl(),
                        fileUrlData.getFileName(), fileUrlData.getRequestHeaders());
                storedPaths.add(urlFileData.getFilePath());
                File file = FileConvert.INSTANCE.toFile(urlFileData);
                file.setUploadType(uploadType);
                file.setOriginName(fileUrlData.getFileName());
                file.setSaveName(urlFileData.getSaveFileName());
                file.setCreateId(createId);
                file.setCreateTime(LocalDateTime.now());
                // 远程导入文件与普通上传执行同一内容与病毒扫描校验。
                fileList.add(this.createFile(file));
            }
        } catch (RuntimeException e) {
            storedPaths.forEach(this::deleteQuietly);
            throw e;
        }
        return fileList.stream().map(File::getId).map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));
    }

    private void deleteQuietly(String filePath) {
        try {
            localStorageService.delete(filePath);
        } catch (Exception ignored) {
            // 文件清理失败不覆盖原始业务异常；定时清理任务可处理孤儿文件。
        }
    }

    /**
     * 文件业务参数校验
     */
    private void validateFile(File file) {
        // 判断文件是否存在
        if (!FileUtil.exist(fileProperties.getBasePath() +
                FileConstants.SEPARATOR + file.getFilePath())) {
            throw new LogicException(ErrorCodeConstants.FILE_PATH_NOT_EXIST);
        }
        Path basePath = Paths.get(fileProperties.getBasePath()).toAbsolutePath().normalize();
        Path filePath = basePath.resolve(file.getFilePath()).normalize();
        if (!filePath.startsWith(basePath)) {
            throw new LogicException(ErrorCodeConstants.FILE_PATH_NOT_EXIST);
        }
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            fileContentValidationService.validate(file.getFileExtension(), inputStream);
        } catch (LogicException e) {
            throw e;
        } catch (Exception e) {
            throw new LogicException(ErrorCodeConstants.FILE_CONTENT_INVALID);
        }
        try (InputStream inputStream = Files.newInputStream(filePath)) {
            fileVirusScanService.scan(inputStream);
        } catch (LogicException e) {
            throw e;
        } catch (Exception e) {
            throw new LogicException(ErrorCodeConstants.FILE_VIRUS_SCAN_FAILED);
        }
    }
}
