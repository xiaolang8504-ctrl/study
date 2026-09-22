package com.study.module.system.file.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.URLUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.api.dto.response.FilePreviewData;
import com.study.api.dto.response.FileSignatureData;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.file.config.FileProperties;
import com.study.module.system.file.config.PreviewConfig;
import com.study.module.system.file.entity.File;
import com.study.module.system.file.mapper.FileMapper;
import com.study.module.system.file.service.DownloadService;
import com.study.module.system.file.service.FileService;
import com.study.module.system.file.service.PreviewFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 文件服务
 */
@Service
public class PreviewFileServiceImpl extends ServiceImpl<FileMapper, File> implements PreviewFileService {

    @Autowired
    FileService fileService;

    @Autowired
    DownloadService downloadService;

    @Autowired
    FileProperties fileProperties;

    /**
     * 预览地址
     */
    @Override
    public FilePreviewData previewUrl(Integer fileId, String uploadType) {
        // 校验文件
        File file = fileService.checkCurrentUserFile(fileId, uploadType);
        FileSignatureData fileSignatureData = downloadService.downSignature(fileId, uploadType);
        String saveName = file.getSaveName().replaceAll("[&=]", "");
        PreviewConfig previewConfig = fileProperties.getPreview();
        if (previewConfig == null) {
            throw new LogicException(ErrorCodeConstants.FILE_PREVIEW_CONFIG_NOT_EXIST);
        }
        String downloadUrl = previewConfig.getDownloadUrl() + "?fullfilename=" + saveName + "&signature=" + fileSignatureData.getSignature();
        //获取过期时间戳
        String expiresTimestamp = Convert.toStr(System.currentTimeMillis() + Long.parseLong(previewConfig.getExpire()) * 1000).substring(0, 10);
        //此token用在预览服务验证 + 本地下载文件下载验证
        String token = SecureUtil.md5(previewConfig.getAccessToken() + expiresTimestamp + saveName);
        String fileUrl = downloadUrl + "&AccessToken=" + token + "&Expires=" + expiresTimestamp;
        String viewUrl = URLUtil.encode(Base64.encode(fileUrl.getBytes()));
        String previewUrl = previewConfig.getPreviewUrl() + "?url=" + viewUrl;
        FilePreviewData filePreviewData = new FilePreviewData();
        filePreviewData.setPreviewUrl(previewUrl);
        return filePreviewData;
    }
}
