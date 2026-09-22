package com.study.module.system.user.util;

import com.study.module.system.user.config.MailConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * 发送邮件
 */
@Slf4j
@Component
public class MailSender {

    public boolean sendEmail(String[] recipients, String subject, String body, MailConfig mailConfig) {
        if (recipients == null || recipients.length == 0 || mailConfig == null
                || isBlank(mailConfig.getHost()) || mailConfig.getPort() == null
                || isBlank(mailConfig.getUserName()) || isBlank(mailConfig.getPassWord())
                || isBlank(mailConfig.getFromAddress())) {
            log.warn("邮件未发送：mail.* 配置或收件人不完整");
            return false;
        }

        // 第一步：设置邮件服务器的属性
        Properties properties = new Properties();
        properties.put("mail.smtp.host", mailConfig.getHost());
        properties.put("mail.smtp.port", String.valueOf(mailConfig.getPort()));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "10000");
        properties.put("mail.smtp.writetimeout", "10000");

        // 第二步：创建会话
        Session session = Session.getInstance(properties, new Authenticator() {
            /**
             * 获取邮件服务器认证信息。
             */
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // 在此处输入发件人的邮箱账号和密码
                return new PasswordAuthentication(mailConfig.getUserName(), mailConfig.getPassWord());
            }
        });

        // 第三步：创建邮件
        try {
            // 使用MimeMessage构建邮件
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailConfig.getFromAddress()));
            message.setSubject(subject);
            message.setText(body);

            // 设置多个收件人
            InternetAddress[] addressList = new InternetAddress[recipients.length];
            for (int i = 0; i < recipients.length; i++) {
                addressList[i] = new InternetAddress(recipients[i]);
            }
            message.addRecipients(Message.RecipientType.TO, addressList);

            // 第四步：发送邮件
            Transport.send(message);
            return true;

        } catch (MessagingException e) {
            log.error("邮件发送失败，subject={}", subject, e);
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
