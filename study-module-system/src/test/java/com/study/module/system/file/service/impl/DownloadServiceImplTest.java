package com.study.module.system.file.service.impl;

import com.study.api.dto.response.FileSignatureData;
import com.study.common.core.constants.RedisKey;
import com.study.common.core.exception.LogicException;
import com.study.module.system.file.config.FileProperties;
import com.study.module.system.file.config.PreviewConfig;
import com.study.module.system.file.config.UploadConfig;
import com.study.module.system.file.entity.File;
import com.study.module.system.file.service.FileService;
import com.study.module.system.file.service.LocalStorageService;
import com.yunshang.budget.common.redis.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 私有文件签名下载安全回归测试。
 */
class DownloadServiceImplTest {

    @Test
    void shouldRejectPathOutsideStorageRootFromDownloadSignature() throws Exception {
        DownloadServiceImpl service = new DownloadServiceImpl();
        RedisService redisService = mock(RedisService.class);
        FileProperties properties = new FileProperties();
        Path root = Files.createTempDirectory("study-download-root");
        properties.setBasePath(root.toString());
        ReflectionTestUtils.setField(service, "redisService", redisService);
        ReflectionTestUtils.setField(service, "fileProperties", properties);
        when(redisService.hasKey(RedisKey.FILE_DOWNLOAD_SIGNATURE + ":bad-signature")).thenReturn(true);
        Map<Object, Object> signatureData = new HashMap<>();
        signatureData.put("filePath", "../outside.txt");
        signatureData.put("originName", "outside.txt");
        when(redisService.hmget(RedisKey.FILE_DOWNLOAD_SIGNATURE + ":bad-signature")).thenReturn(signatureData);

        assertThrows(LogicException.class,
                () -> service.download("bad-signature", new MockHttpServletResponse()));
    }

    @Test
    void shouldCheckExplicitOwnerBeforeCreatingAsyncDownloadUrl() {
        DownloadServiceImpl service = new DownloadServiceImpl();
        FileService fileService = mock(FileService.class);
        LocalStorageService localStorageService = mock(LocalStorageService.class);
        FileProperties properties = new FileProperties();
        UploadConfig uploadConfig = new UploadConfig();
        uploadConfig.setExpire("60");
        properties.setUpload(Collections.singletonMap("default", uploadConfig));
        PreviewConfig preview = new PreviewConfig();
        preview.setDownloadUrl("https://download.example.test/api/file/downloadFile");
        properties.setPreview(preview);
        ReflectionTestUtils.setField(service, "fileService", fileService);
        ReflectionTestUtils.setField(service, "localStorageService", localStorageService);
        ReflectionTestUtils.setField(service, "fileProperties", properties);
        File file = new File();
        file.setId(10);
        file.setOriginName("page.png");
        file.setFilePath("wrongQuestion/20260914/page.png");
        when(fileService.checkUserFile(10, "wrongQuestion", 7L)).thenReturn(file);
        FileSignatureData signatureData = new FileSignatureData();
        signatureData.setSignature("short-lived-signature");
        when(localStorageService.downSignature(anyString(), anyString(), anyString())).thenReturn(signatureData);

        String url = service.downUrlByUserId(10, "wrongQuestion", 7L);

        assertEquals("https://download.example.test/api/file/downloadFile?signature=short-lived-signature", url);
        verify(fileService).checkUserFile(10, "wrongQuestion", 7L);
        verify(localStorageService).downSignature(eq("page.png"), eq("wrongQuestion/20260914/page.png"), eq("60"));
    }
}
