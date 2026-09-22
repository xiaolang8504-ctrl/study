package com.study.common.core.constants;

/**
 * RedisKye统一使用常量格式命名，同一个模块或服务下的Key使用相同前缀，层级关系使用冒号‘:’衔接；
 * 在Redis中，使用":"衔接的键名，会自动分配至同一个命名空间
 */
public class RedisKey {

    /**
     * TOKEN
     */
    public static final String SYSTEM_USER_TOKEN = "SYSTEM:USER:TOKEN";

    /**
     * REFRESH_TOKEN
     */
    public static final String SYSTEM_USER_REFRESH_TOKEN = "SYSTEM:USER:REFRESH_TOKEN";

    /**
     * 登录滑块验证码
     */
    public static final String SYSTEM_USER_SLIDER_CAPTCHA = "SYSTEM:USER:SLIDER:CAPTCHA";

    /**
     * 已通过校验、等待登录消费的滑块验证码
     */
    public static final String SYSTEM_USER_SLIDER_CAPTCHA_VERIFIED = "SYSTEM:USER:SLIDER:CAPTCHA:VERIFIED";

    /**
     * 上传签名
     */
    public static final String FILE_UPLOAD_SIGNATURE = "FILE:UPLOAD:SIGNATURE";

    /**
     * 下载签名
     */
    public static final String FILE_DOWNLOAD_SIGNATURE = "FILE:DOWNLOAD:SIGNATURE";

    /**
     * 京东 TOKEN
     */
    public static final String BRIDGE_JD_TOKEN = "BRIDGE:JD:TOKEN";

}
