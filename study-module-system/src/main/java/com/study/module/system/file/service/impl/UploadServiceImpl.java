package com.study.module.system.file.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import com.study.common.core.constants.RedisKey;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.redis.RedisService;
import com.study.module.system.file.config.FileProperties;
import com.study.module.system.file.config.UploadConfig;
import com.study.module.system.file.domain.LocalFileData;
import com.study.module.system.file.dto.request.BatchUploadReq;
import com.study.module.system.file.dto.request.UploadReq;
import com.study.module.system.file.dto.response.FileUploadPolicyResp;
import com.study.module.system.file.dto.response.UploadFileResp;
import com.study.module.system.file.entity.File;
import com.study.module.system.file.service.DownloadService;
import com.study.module.system.file.service.FileService;
import com.study.module.system.file.service.LocalStorageService;
import com.study.module.system.file.service.UploadService;
import com.study.module.system.file.service.FileContentValidationService;
import com.study.module.system.file.service.FileVirusScanService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 上传服务
 */
@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    LocalStorageService localStorageService;

    @Autowired
    FileProperties fileProperties;

    @Autowired
    FileService fileService;

    @Autowired
    DownloadService downloadService;

    @Autowired
    RedisService redisService;

    @Autowired
    FileContentValidationService fileContentValidationService;

    @Autowired
    FileVirusScanService fileVirusScanService;

    /**
     * 默认的文件名最大长度 50
     */
    private static final int DEFAULT_FILE_NAME_LENGTH = 200;

    /**
     * 上传签名生成
     */
    @Override
    public FileUploadPolicyResp policy(String uploadType) {

        // 校验上传类型
        UploadConfig uploadConfig = checkUploadType(uploadType);

        // 最大允许的文件大小
        int maxSize = Integer.parseInt(uploadConfig.getMaxSize());
        // 允许文件的扩展名
        String allowedExtension = uploadConfig.getAllowedExtension();
        // 文件大小
        long maxUploadSize = maxSize * 1024 * 1024L;

        // 上传签名
        String signature = SecureUtil.md5(uploadType +
                allowedExtension + maxUploadSize + IdUtil.simpleUUID());
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("uploadType", uploadType);
        keyMap.put("allowedExtension", allowedExtension);
        keyMap.put("maxUploadSize", maxUploadSize);
        // 上传凭证仅能由签发它的登录用户使用，避免短期凭证在账号之间被重放。
        keyMap.put("userId", AccountUtils.getUserId());
        redisService.hmset(
                RedisKey.FILE_UPLOAD_SIGNATURE + ":" + signature, keyMap, Long.valueOf(uploadConfig.getExpire())
        );

        // 提交节点
        FileUploadPolicyResp response = new FileUploadPolicyResp();
        response.setSignature(signature);
        return response;
    }

    /**
     * 上传文件
     */
    @Override
    public UploadFileResp upload(UploadReq request) {

        UploadPolicy uploadPolicy = getUploadPolicy(request.getSignature());
        String uploadType = uploadPolicy.uploadType;
        String allowedExtension = uploadPolicy.allowedExtension;
        long maxUploadSize = uploadPolicy.maxUploadSize;

        // 获取文件
        MultipartFile file = request.getFile();
        // 获取文件名
        String originalName = ObjectUtil.isEmpty(request.getFileName()) ? file.getOriginalFilename() : request.getFileName();
        // 获取文件扩展名
        String extName = FileUtil.extName(originalName);
        // 获取文件大小
        long size = file.getSize();

        // 校验文件名
        checkFileName(originalName);
        // 校验文件大小
        checkFileSize(size, maxUploadSize);
        // 校验文件扩展名
        checkFileExtension(extName, allowedExtension.split(","));
        // 校验真实文件头，防止仅伪造扩展名绕过白名单。
        validateFileContent(file, extName);

        // 上传文件
        LocalFileData localFileData;
        try {
            localFileData = localStorageService.upload(uploadType, file.getInputStream(), originalName);
        } catch (IOException e) {
            throw new LogicException(ErrorCodeConstants.UPLOAD_FILE_FAIL);
        }
        // 组装文件入库
        File attachment = fileService.assembleFile(uploadType, localFileData, originalName, extName, size,
                AccountUtils.getUserId());

        // 返回上传数据
        UploadFileResp response = new UploadFileResp();
        response.setId(attachment.getId());
        response.setOriginName(originalName);
        response.setFileExtension(extName);
        response.setFileSize(FileUtil.readableFileSize(size));
        return response;
    }

    /**
     * 批量上传文件
     */
    @Override
    public List<UploadFileResp> batchUploadFile(BatchUploadReq request) {
        List<UploadFileResp> list = new ArrayList<>();
        UploadPolicy uploadPolicy = getUploadPolicy(request.getSignature());
        String uploadType = uploadPolicy.uploadType;
        String allowedExtension = uploadPolicy.allowedExtension;
        long maxUploadSize = uploadPolicy.maxUploadSize;

        for (MultipartFile file : request.getFiles()) {
            // 获取文件名
            String originalName = file.getOriginalFilename();
            // 获取文件扩展名
            String extName = FileUtil.extName(originalName);
            // 获取文件大小
            long size = file.getSize();

            // 校验文件名
            checkFileName(originalName);
            // 校验文件大小
            checkFileSize(size, maxUploadSize);
            // 校验文件扩展名
            checkFileExtension(extName, allowedExtension.split(","));
            // 校验真实文件头，防止仅伪造扩展名绕过白名单。
            validateFileContent(file, extName);

            // 上传文件
            LocalFileData localFileData;
            try {
                localFileData = localStorageService.upload(uploadType, file.getInputStream(), originalName);
            } catch (IOException e) {
                throw new LogicException(ErrorCodeConstants.UPLOAD_FILE_FAIL);
            }
            // 组装文件入库
            File attachment = fileService.assembleFile(uploadType, localFileData, originalName, extName, size,
                    AccountUtils.getUserId());
            UploadFileResp response = new UploadFileResp();
            response.setId(attachment.getId());
            response.setOriginName(originalName);
            response.setFileExtension(extName);
            response.setFileSize(FileUtil.readableFileSize(size));
            list.add(response);
        }
        return list;
    }

    /**
     * 校验上传类型
     */
    private UploadConfig checkUploadType(String uploadType) {
        UploadConfig uploadConfig = fileProperties.getUpload().getOrDefault(uploadType, fileProperties.getUpload().get("default"));
        if (uploadConfig == null) {
            throw new LogicException(ErrorCodeConstants.FILE_UPLOAD_TYPE_CONFIG_NOT_EXIST);
        }
        return uploadConfig;
    }

    /**
     * 校验文件名
     */
    private void checkFileName(String originalName) {
        if (originalName != null && originalName.length() > DEFAULT_FILE_NAME_LENGTH) {
            throw new LogicException(ErrorCodeConstants.FILE_NAME_LENGTH_TOO_LONG);
        }
    }

    /**
     * 校验文件大小
     */
    private void checkFileSize(long size, long maxUploadSize) {
        if (size > maxUploadSize) {
            throw new LogicException(ErrorCodeConstants.UPLOAD_FILE_TOO_LARGE);
        }
    }

    /**
     * 读取并校验 Redis 中的上传策略。Redis 序列化可能将数字还原为 Integer、Long
     * 或 String，因此统一按 Number/String 解析，避免有效策略因类型差异失效。
     */
    private UploadPolicy getUploadPolicy(String signature) {
        String redisKey = RedisKey.FILE_UPLOAD_SIGNATURE + ":" + signature;
        if (!redisService.hasKey(redisKey)) {
            throw new LogicException(ErrorCodeConstants.FILE_SIGNATURE_INVALID);
        }
        Map<Object, Object> keyMap = redisService.hmget(redisKey);
        String uploadType = asString(keyMap.get("uploadType"));
        String allowedExtension = asString(keyMap.get("allowedExtension"));
        Long maxUploadSize = asLong(keyMap.get("maxUploadSize"));
        Long ownerUserId = asLong(keyMap.get("userId"));
        if (ObjectUtil.hasEmpty(uploadType, allowedExtension, maxUploadSize, ownerUserId)
                || !ownerUserId.equals(AccountUtils.getUserId())) {
            throw new LogicException(ErrorCodeConstants.FILE_SIGNATURE_INVALID);
        }
        return new UploadPolicy(uploadType, allowedExtension, maxUploadSize);
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Long asLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return value == null ? null : Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static class UploadPolicy {
        private final String uploadType;
        private final String allowedExtension;
        private final long maxUploadSize;

        private UploadPolicy(String uploadType, String allowedExtension, long maxUploadSize) {
            this.uploadType = uploadType;
            this.allowedExtension = allowedExtension;
            this.maxUploadSize = maxUploadSize;
        }
    }

    /**
     * 校验文件扩展名
     */
    public void checkFileExtension(String extName, String[] allowedExtension) {
        boolean flag = false;
        for (String str : allowedExtension) {
            if (str.equalsIgnoreCase(extName)) {
                flag = true;
                break;
            }
        }
        if (!flag) {
            throw new LogicException(ErrorCodeConstants.FILE_EXT_ERROR);
        }
    }

    /**
     * 验证上传文件实际内容，MultipartFile 可重复获取输入流，校验后再交由存储层写入。
     */
    private void validateFileContent(MultipartFile file, String extName) {
        try (java.io.InputStream inputStream = file.getInputStream()) {
            fileContentValidationService.validate(extName, inputStream);
        } catch (LogicException e) {
            throw e;
        } catch (IOException e) {
            throw new LogicException(ErrorCodeConstants.UPLOAD_FILE_FAIL);
        }
        try (java.io.InputStream inputStream = file.getInputStream()) {
            fileVirusScanService.scan(inputStream);
        } catch (LogicException e) {
            throw e;
        } catch (IOException e) {
            throw new LogicException(ErrorCodeConstants.UPLOAD_FILE_FAIL);
        }
    }
}
