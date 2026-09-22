package com.study.module.system.user.service;

public interface EmailService {

    /**
     * 发送邮件
     */
    void sendEmail(String msgType,String formEmail,String str);
}