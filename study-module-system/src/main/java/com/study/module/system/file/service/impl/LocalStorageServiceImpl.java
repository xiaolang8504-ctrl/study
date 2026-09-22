package com.study.module.system.file.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.study.api.dto.response.FileSignatureData;
import com.study.common.core.constants.RedisKey;
import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.yunshang.budget.common.redis.RedisService;
import com.study.module.system.file.config.FileProperties;
import com.study.module.system.file.contants.FileConstants;
import com.study.module.system.file.domain.LocalFileData;
import com.study.module.system.file.domain.UrlFileData;
import com.study.module.system.file.service.LocalStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 本地文件存储
 */
@Component
public class LocalStorageServiceImpl implements LocalStorageService {

    private static final int URL_FILE_DOWNLOAD_TIMEOUT = 30000;
    private static final String BROWSER_USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0 Safari/537.36";

    @Autowired
    FileProperties fileProperties;

    @Autowired
    RedisService redisService;

    /**
     * 创建bucket
     */
    @Override
    public String createBucket(String bucketName) {
        String relativePath = bucketName +
                FileConstants.SEPARATOR + genDateMkdir(FileConstants.DATE_FORMAT);
        String dirPath = fileProperties.getBasePath() + FileConstants.SEPARATOR + relativePath;
        FileUtil.mkdir(dirPath);
        return dirPath;
    }

    /**
     * 上传文件
     */
    @Override
    public LocalFileData upload(String bucketName, InputStream fileInputStream, String fileName) {
        // 当 Bucket 不存在时创建
        String dir = fileProperties.getBasePath() + FileConstants.SEPARATOR + bucketName;
        if (!FileUtil.isDirectory(fileProperties.getBasePath() + FileConstants.SEPARATOR + bucketName)) {
            createBucket(bucketName);
        }
        // 写入文件
        String saveFileName = IdUtil.simpleUUID() + "." + FileUtil.extName(fileName);
        String objectName = genDateMkdir(FileConstants.DATE_FORMAT) + saveFileName;
        String filePath = dir + FileConstants.SEPARATOR + objectName;
        try {
            FileUtil.writeFromStream(fileInputStream, FileUtil.file(filePath));
        } catch (RuntimeException exception) {
            // 写入失败时删除未登记数据库的半成品，避免采集重试留下临时残片。
            FileUtil.del(FileUtil.file(filePath));
            throw exception;
        }
        LocalFileData localFileData = new LocalFileData();
        localFileData.setFilePath(bucketName + FileConstants.SEPARATOR + objectName);
        localFileData.setSaveFileName(saveFileName);
        return localFileData;
    }

    /**
     * 下载凭证
     */
    @Override
    public FileSignatureData downSignature(String originName, String filePath, String expires) {
        Path basePath = Paths.get(fileProperties.getBasePath()).toAbsolutePath().normalize();
        Path targetPath = basePath.resolve(filePath).normalize();
        if (!targetPath.startsWith(basePath) || !Files.isRegularFile(targetPath)) {
            throw new LogicException(ErrorCodeConstants.FILE_PATH_NOT_EXIST);
        }
        //此下载凭证用在本地下载文件下载验证
        File file = targetPath.toFile();
        String signature = SecureUtil.md5(file.getName() + IdUtil.simpleUUID());
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("filePath", filePath);
        keyMap.put("originName", originName);
        Boolean result = redisService.hmset(RedisKey.FILE_DOWNLOAD_SIGNATURE + ":" + signature, keyMap, Long.valueOf(expires));
        if (!result) {
            throw new LogicException(ErrorCodeConstants.FILE_SIGNATURE_INVALID);
        }
        FileSignatureData resp = new FileSignatureData();
        resp.setSignature(signature);
        // 返回文件下载地址
        return resp;
    }

    /**
     * 生成文件
     */
    @Override
    public UrlFileData generateUrlFile(String uploadType, String fileUrl, String fileName) {
        return generateUrlFile(uploadType, fileUrl, fileName, null);
    }

    /**
     * 生成文件
     */
    @Override
    public UrlFileData generateUrlFile(String uploadType, String fileUrl, String fileName, Map<String, String> requestHeaders) {
        // 当 Bucket 不存在时创建
        String dir = fileProperties.getBasePath() + FileConstants.SEPARATOR + uploadType;
        if (!FileUtil.isDirectory(fileProperties.getBasePath() + FileConstants.SEPARATOR + uploadType)) {
            createBucket(uploadType);
        }
        // 写入文件
        // 远程来源的文件名不参与路径构造，避免 URL 导入时出现目录穿越。
        String extension = FileUtil.extName(fileName);
        String saveFileName = IdUtil.simpleUUID() + (extension.isEmpty() ? "" : "." + extension);
        String objectName = genDateMkdir(FileConstants.DATE_FORMAT) + saveFileName;
        String filePath = dir + FileConstants.SEPARATOR + objectName;
        long size = downloadUrlFile(fileUrl, FileUtil.file(filePath), requestHeaders);
        UrlFileData urlFileData = new UrlFileData();
        urlFileData.setFilePath(uploadType + FileConstants.SEPARATOR + objectName);
        urlFileData.setSaveFileName(saveFileName);
        urlFileData.setFileExtension(FileUtil.extName(fileName));
        urlFileData.setFileBytes(Math.toIntExact(size));
        urlFileData.setFileSize(FileUtil.readableFileSize(size));
        return urlFileData;
    }

    /**
     * 删除本地存储文件，仅允许删除文件根目录内的资源
     */
    @Override
    public void delete(String filePath) {
        try {
            Path basePath = Paths.get(fileProperties.getBasePath()).toAbsolutePath().normalize();
            Path targetPath = basePath.resolve(filePath).normalize();
            if (!targetPath.startsWith(basePath)) {
                throw new LogicException(ErrorCodeConstants.FILE_DELETE_FAIL);
            }
            Files.deleteIfExists(targetPath);
        } catch (LogicException e) {
            throw e;
        } catch (Exception e) {
            throw new LogicException(ErrorCodeConstants.FILE_DELETE_FAIL);
        }
    }

    /**
     * 下载远程URL文件
     */
    private long downloadUrlFile(String fileUrl, File targetFile, Map<String, String> requestHeaders) {
        HttpRequest request = HttpRequest.get(fileUrl)
                .timeout(URL_FILE_DOWNLOAD_TIMEOUT)
                .setFollowRedirects(true)
                .setMaxRedirectCount(5)
                .header("User-Agent", BROWSER_USER_AGENT)
                .header("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Referer", buildReferer(fileUrl));
        if (requestHeaders != null) {
            requestHeaders.forEach(request::header);
        }
        try (HttpResponse response = request.execute()) {
            int status = response.getStatus();
            if (status < 200 || status >= 300) {
                throw new LogicException(ErrorCodeConstants.DOWNLOAD_FILE_FAIL);
            }
            return response.writeBody(targetFile);
        } catch (LogicException e) {
            FileUtil.del(targetFile);
            throw e;
        } catch (HttpException e) {
            FileUtil.del(targetFile);
            throw new LogicException(ErrorCodeConstants.DOWNLOAD_FILE_FAIL);
        } catch (Exception e) {
            FileUtil.del(targetFile);
            throw new LogicException(ErrorCodeConstants.DOWNLOAD_FILE_FAIL);
        }
    }

    /**
     * 生成下载来源页
     */
    private String buildReferer(String fileUrl) {
        try {
            URI uri = URI.create(fileUrl);
            if (uri.getScheme() == null || uri.getHost() == null) {
                return fileUrl;
            }
            return uri.getScheme() + "://" + uri.getHost() + "/";
        } catch (Exception e) {
            return fileUrl;
        }
    }

    /**
     * 生成指定格式的目录名称(日期格式)
     */
    public String genDateMkdir(String format) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format)) + FileConstants.SEPARATOR;
    }
}
