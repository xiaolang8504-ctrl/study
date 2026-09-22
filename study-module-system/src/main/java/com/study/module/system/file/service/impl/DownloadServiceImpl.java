package com.study.module.system.file.service.impl;

import cn.hutool.core.io.FileUtil;
import com.study.api.dto.response.FileSignatureData;
import com.study.common.core.constants.RedisKey;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.redis.RedisService;
import com.study.module.system.file.config.FileProperties;
import com.study.module.system.file.contants.FileConstants;
import com.study.module.system.file.entity.File;
import com.study.module.system.file.service.DownloadService;
import com.study.module.system.file.service.FileService;
import com.study.module.system.file.service.LocalStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 下载服务
 */
@Service
@Slf4j
public class DownloadServiceImpl implements DownloadService {
    @Autowired
    FileService fileService;

    @Autowired
    FileProperties fileProperties;

    @Autowired
    LocalStorageService localStorageService;

    @Autowired
    RedisService redisService;

    /**
     * 下载文件
     */
    @Override
    public void download(String signature, HttpServletResponse response) {
        if (!redisService.hasKey(RedisKey.FILE_DOWNLOAD_SIGNATURE + ":" + signature)) {
            throw new LogicException(ErrorCodeConstants.FILE_SIGNATURE_INVALID);
        }
        Map<Object, Object> keyMap = redisService.hmget(RedisKey.FILE_DOWNLOAD_SIGNATURE + ":" + signature);
        String filePath = (String) keyMap.get("filePath");
        String originName = (String) keyMap.get("originName");
        Path basePath = Paths.get(fileProperties.getBasePath()).toAbsolutePath().normalize();
        Path downloadPath = basePath.resolve(filePath).normalize();
        if (!downloadPath.startsWith(basePath)) {
            log.warn("拒绝越界下载路径，signature={}", signature);
            throw new LogicException(ErrorCodeConstants.FILE_PATH_NOT_EXIST);
        }
        // 校验下载链接TOKEN
        downFile(originName, downloadPath.toString(), response);
    }

    /**
     * 下载凭证
     */
    @Override
    public FileSignatureData downSignature(Integer id, String uploadType) {
        // 签名下载令牌同样必须先通过当前用户（或管理员）授权，不能作为通用文件查询接口。
        File file = fileService.checkCurrentUserFile(id, uploadType);
        // 获取文件URL
        return localStorageService.downSignature(file.getOriginName(), file.getFilePath(),
                fileProperties.getUpload().getOrDefault(uploadType, fileProperties.getUpload().get("default")).getExpire()
        );
    }

    /**
     * 下载地址
     */
    @Override
    public String downUrl(Integer fileId, String uploadType) {
        File file = fileService.checkCurrentUserFile(fileId, uploadType);
        return createDownloadUrl(file, uploadType);
    }

    /**
     * 没有 HTTP 安全上下文的任务必须显式传入任务所属用户；这里仍通过文件服务
     * 校验归属，避免后台 OCR、净化任务绕过私有文件访问控制。
     */
    @Override
    public String downUrlByUserId(Integer fileId, String uploadType, Long userId) {
        File file = fileService.checkUserFile(fileId, uploadType, userId);
        return createDownloadUrl(file, uploadType);
    }

    private String createDownloadUrl(File file, String uploadType) {
        FileSignatureData fileSignatureData = localStorageService.downSignature(file.getOriginName(), file.getFilePath(),
                fileProperties.getUpload().getOrDefault(uploadType, fileProperties.getUpload().get("default")).getExpire());
        return fileProperties.getPreview().getDownloadUrl() + "?signature=" + fileSignatureData.getSignature();
    }

    /**
     * 下载地址
     */
    @Override
    public String downUrl(String signature) {
        return fileProperties.getPreview().getDownloadUrl() + "?signature=" + signature;
    }

    /**
     * 下载文件
     */
    private void downFile(String originName, String filePath, HttpServletResponse response) {
        // 校验文件路径
        if (!FileUtil.exist(filePath)) {
            throw new LogicException(ErrorCodeConstants.FILE_PATH_NOT_EXIST);
        }
        try {
            // 设置HTTP头信息
            setHttpHeader(originName, response);
            // 写入字节流
            writeBytes(filePath, response.getOutputStream());
        } catch (IOException e) {
            // 二进制响应已设置 Content-Type 后不能再交由全局异常处理器写 JSON，否则会出现 PDF/图片类型无法序列化 Result 的异常。
            log.error("下载文件失败，filePath={}", filePath, e);
            if (!response.isCommitted()) {
                response.reset();
                try {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "下载文件失败");
                } catch (IOException ignored) {
                    log.warn("下载失败响应写入异常，filePath={}", filePath, ignored);
                }
            }
        }
    }

    /**
     * 设置HTTP头信息
     */
    private void setHttpHeader(String name, HttpServletResponse response) throws UnsupportedEncodingException {
        // 对字符串进行百分比编码，将特殊字符转换成URL编码形式
        String percentEncodedFileName = percentEncode(name);
        String contentType = URLConnection.guessContentTypeFromName(name);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        String dispositionType = contentType.startsWith("image/") ? "inline" : "attachment";
        // 设置响应头
        String contentDispositionValue = dispositionType + "; filename=" +
                percentEncodedFileName +
                ";" +
                "filename*=" +
                "utf-8''" +
                percentEncodedFileName;
        response.setContentType(contentType);
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition,download-filename");
        response.setHeader("Content-disposition", contentDispositionValue);
        response.setHeader("download-filename", percentEncodedFileName);
    }

    /**
     * 对字符串进行百分比编码，将特殊字符转换成URL编码形式
     */
    public String percentEncode(String s) throws UnsupportedEncodingException {
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8.toString());
        return encode.replaceAll("\\+", "%20");
    }

    /**
     * 写入字节流
     */
    public void writeBytes(String filePath, OutputStream os) throws IOException {
        try (FileInputStream fis = new FileInputStream(FileUtil.file(filePath))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw e;
        }
    }
}
