package com.study.module.system.user.util;

import com.study.module.system.user.config.MailConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class MailSenderTest {

    @Test
    void shouldNotClaimSuccessWhenMailConfigurationIsIncomplete() {
        assertFalse(new MailSender().sendEmail(new String[]{"guardian@example.com"}, "周报", "内容", new MailConfig()));
    }
}
