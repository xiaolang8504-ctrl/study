package com.study.module.system.file.service.impl;

import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.file.config.FileProperties;
import com.study.module.system.file.config.FileSecurityConfig;
import com.study.module.system.file.service.FileVirusScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * ClamAV INSTREAM 协议扫描实现。
 */
@Slf4j
@Service
public class ClamAvFileVirusScanServiceImpl implements FileVirusScanService {

    private static final byte[] INSTREAM_COMMAND = "zINSTREAM\0".getBytes(StandardCharsets.US_ASCII);
    private static final int BUFFER_SIZE = 8192;

    @Autowired
    private FileProperties fileProperties;

    @Override
    public void scan(InputStream inputStream) {
        FileSecurityConfig securityConfig = fileProperties.getSecurity();
        if (securityConfig == null || !Boolean.TRUE.equals(securityConfig.getVirusScanEnabled())) {
            return;
        }
        try {
            String result = scanByClamAv(inputStream, securityConfig);
            if (!result.contains("OK")) {
                log.warn("ClamAV 拒绝上传文件，result={}", result);
                throw new LogicException(ErrorCodeConstants.FILE_VIRUS_DETECTED);
            }
        } catch (LogicException e) {
            throw e;
        } catch (Exception e) {
            if (Boolean.TRUE.equals(securityConfig.getVirusScanFailOpen())) {
                log.error("ClamAV 扫描服务异常，按 fail-open 配置继续上传", e);
                return;
            }
            log.error("ClamAV 扫描服务异常，拒绝上传", e);
            throw new LogicException(ErrorCodeConstants.FILE_VIRUS_SCAN_FAILED);
        }
    }

    private String scanByClamAv(InputStream inputStream, FileSecurityConfig securityConfig) throws IOException {
        try (Socket socket = new Socket()) {
            int timeout = securityConfig.getVirusScanTimeout() == null ? 10000 : securityConfig.getVirusScanTimeout();
            socket.connect(new InetSocketAddress(securityConfig.getVirusScanHost(), securityConfig.getVirusScanPort()), timeout);
            socket.setSoTimeout(timeout);
            try (DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                 InputStream responseStream = socket.getInputStream()) {
                outputStream.write(INSTREAM_COMMAND);
                byte[] buffer = new byte[BUFFER_SIZE];
                int readLength;
                while ((readLength = inputStream.read(buffer)) != -1) {
                    outputStream.writeInt(readLength);
                    outputStream.write(buffer, 0, readLength);
                }
                outputStream.writeInt(0);
                outputStream.flush();
                return readResponse(responseStream);
            }
        }
    }

    private String readResponse(InputStream responseStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[256];
        int readLength;
        while ((readLength = responseStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, readLength);
            if (buffer[readLength - 1] == 0 || buffer[readLength - 1] == '\n') {
                break;
            }
        }
        String result = outputStream.toString(StandardCharsets.UTF_8.name()).trim();
        if (result.isEmpty()) {
            throw new IOException("ClamAV 未返回扫描结果");
        }
        return result;
    }
}
