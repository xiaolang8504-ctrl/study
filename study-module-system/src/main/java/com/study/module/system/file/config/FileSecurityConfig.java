package com.study.module.system.file.config;

import lombok.Data;

/**
 * 文件安全扫描配置。
 */
@Data
public class FileSecurityConfig {

    /** 是否启用 ClamAV INSTREAM 病毒扫描。 */
    private Boolean virusScanEnabled = Boolean.FALSE;

    /** ClamAV 服务地址。 */
    private String virusScanHost = "127.0.0.1";

    /** ClamAV clamd 监听端口。 */
    private Integer virusScanPort = 3310;

    /** 连接与读取超时，毫秒。 */
    private Integer virusScanTimeout = 10000;

    /**
     * 扫描服务异常时是否允许文件继续流转。生产环境应保持 false，避免安全能力降级为放行。
     */
    private Boolean virusScanFailOpen = Boolean.FALSE;
}
