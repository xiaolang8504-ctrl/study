package com.study.module.system.guardian.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.study.module.system.guardian.dto.response.GuardianWeeklyReportResp;
import com.study.module.system.guardian.entity.GuardianWeeklyReportSubscription;
import com.study.module.system.guardian.mapper.StudentGuardianRelMapper;
import com.study.module.system.guardian.service.GuardianWeeklyReportService;
import com.study.module.system.guardian.service.GuardianWeeklyReportSubscriptionService;
import com.study.module.system.msg.service.MsgService;
import com.study.module.system.user.config.MailConfig;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.service.UserService;
import com.study.module.system.user.util.MailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 周报各投递通道分别确认，邮件失败不会被站内消息成功掩盖。 */
@ExtendWith(MockitoExtension.class)
class GuardianWeeklyReportDispatchServiceImplTest {

    @Mock private GuardianWeeklyReportSubscriptionService subscriptionService;
    @Mock private StudentGuardianRelMapper studentGuardianRelMapper;
    @Mock private MsgService msgService;
    @Mock private UserService userService;
    @Mock private GuardianWeeklyReportService guardianWeeklyReportService;
    @Mock private MailConfig mailConfig;
    @Mock private MailSender mailSender;
    @InjectMocks private GuardianWeeklyReportDispatchServiceImpl dispatchService;

    @Test
    void shouldRetryFailedEmailEvenWhenSiteMessageSucceeded() {
        GuardianWeeklyReportSubscription subscription = subscription(1, 1);
        LocalDate week = LocalDate.of(2026, 9, 7);
        prepareActiveSubscription(subscription);
        when(msgService.save(any())).thenReturn(true);
        when(mailSender.sendEmail(any(), anyString(), anyString(), any(MailConfig.class))).thenReturn(false);

        dispatchService.dispatchWeeklyReportReminder(week);

        assertEquals(week, subscription.getSiteLastSentWeek());
        assertNull(subscription.getEmailLastSentWeek());
        assertNull(subscription.getLastSentWeek());
        verify(subscriptionService).updateById(subscription);
    }

    @Test
    void shouldMarkEmailDeliveredOnlyAfterMailSenderSucceeds() {
        GuardianWeeklyReportSubscription subscription = subscription(0, 1);
        LocalDate week = LocalDate.of(2026, 9, 7);
        prepareActiveSubscription(subscription);
        when(mailSender.sendEmail(any(), anyString(), anyString(), any(MailConfig.class))).thenReturn(true);

        dispatchService.dispatchWeeklyReportReminder(week);

        assertEquals(week, subscription.getEmailLastSentWeek());
        assertEquals(week, subscription.getLastSentWeek());
        verify(subscriptionService).updateById(subscription);
    }

    @Test
    void shouldNotPersistAnyDeliveryWhenEmailFailsWithoutSiteChannel() {
        GuardianWeeklyReportSubscription subscription = subscription(0, 1);
        prepareActiveSubscription(subscription);
        when(mailSender.sendEmail(any(), anyString(), anyString(), any(MailConfig.class))).thenReturn(false);

        dispatchService.dispatchWeeklyReportReminder(LocalDate.of(2026, 9, 7));

        assertNull(subscription.getEmailLastSentWeek());
        verify(subscriptionService, never()).updateById(subscription);
    }

    private GuardianWeeklyReportSubscription subscription(int siteEnabled, int emailEnabled) {
        GuardianWeeklyReportSubscription subscription = new GuardianWeeklyReportSubscription();
        subscription.setGuardianUserId(20L);
        subscription.setStudentUserId(10L);
        subscription.setSiteNotificationEnabled(siteEnabled);
        subscription.setEmailEnabled(emailEnabled);
        return subscription;
    }

    private void prepareActiveSubscription(GuardianWeeklyReportSubscription subscription) {
        User guardian = new User();
        guardian.setEmail("guardian@example.com");
        GuardianWeeklyReportResp report = new GuardianWeeklyReportResp();
        report.setStudentName("学生A");
        report.setNewWrongQuestionCount(1);
        report.setCorrectedCount(1);
        report.setEffectiveReviewCount(1);
        report.setRetentionRate(100);
        report.setOverdueCount(0);
        report.setNextWeekSuggestion("保持复习节奏");
        when(subscriptionService.list()).thenReturn(Collections.singletonList(subscription));
        when(studentGuardianRelMapper.selectCount(any(Wrapper.class))).thenReturn(1L);
        when(guardianWeeklyReportService.buildWeeklyReport(10L)).thenReturn(report);
        when(userService.getById(20L)).thenReturn(guardian);
        when(mailConfig.getHost()).thenReturn("smtp.example.com");
    }
}
