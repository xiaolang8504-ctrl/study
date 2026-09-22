package com.study.module.system.user.service.impl;

import cn.hutool.core.text.StrFormatter;
import com.study.module.system.msg.config.MessageConfig;
import com.study.module.system.msg.config.MsgConfig;
import com.study.module.system.user.config.MailConfig;
import com.study.module.system.user.service.EmailService;
import com.study.module.system.user.util.MailSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 */
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    MailConfig mailConfig;

    @Autowired
    MsgConfig msgConfig;

    @Autowired
    MailSender mailSender;

    /**
     * 发送邮件
     */
    @Override
    public void sendEmail(String msgType,String formEmail,String str) {
        MessageConfig messageConfig = msgConfig.getMsgType().get(msgType);
        String[] recipients = {formEmail};
        String subject = messageConfig.getTitle();
        String body = StrFormatter.format(messageConfig.getContent(), str);
        mailSender.sendEmail(recipients, subject, body,mailConfig);
    }
}
