package com.study.module.system.guardian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.study.module.system.guardian.constants.GuardianBindingStatus;
import com.study.module.system.guardian.entity.GuardianWeeklyReportSubscription;
import com.study.module.system.guardian.entity.StudentGuardianRel;
import com.study.module.system.guardian.mapper.StudentGuardianRelMapper;
import com.study.module.system.guardian.service.GuardianWeeklyReportSubscriptionService;
import com.study.module.system.guardian.service.GuardianWeeklyReportService;
import com.study.module.system.guardian.dto.response.GuardianWeeklyReportResp;
import com.study.module.system.msg.constants.Read;
import com.study.module.system.msg.entity.Msg;
import com.study.module.system.msg.service.MsgService;
import com.study.module.system.user.entity.User;
import com.study.module.system.user.service.UserService;
import com.study.module.system.user.config.MailConfig;
import com.study.module.system.user.util.MailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 每周向已订阅且仍保持有效绑定的家长发送站内周报提醒。 */
@Slf4j
@Service
public class GuardianWeeklyReportDispatchServiceImpl {
    @Autowired private GuardianWeeklyReportSubscriptionService subscriptionService;
    @Autowired private StudentGuardianRelMapper studentGuardianRelMapper;
    @Autowired private MsgService msgService;
    @Autowired private UserService userService;
    @Autowired private GuardianWeeklyReportService guardianWeeklyReportService;
    @Autowired private MailConfig mailConfig;
    @Autowired private MailSender mailSender;

    @Scheduled(cron = "${guardian.weekly-report.cron:0 0 8 ? * MON}")
    public void dispatchWeeklyReportReminder() {
        dispatchWeeklyReportReminder(LocalDate.now().with(java.time.DayOfWeek.MONDAY));
    }

    /**
     * 同一周内补偿失败的邮件，站内消息和邮件以各自的送达结果为准，互不掩盖。
     */
    @Scheduled(cron = "${guardian.weekly-report.retry-cron:0 0 9-20 ? * MON}")
    public void retryWeeklyReportReminder() {
        dispatchWeeklyReportReminder(LocalDate.now().with(java.time.DayOfWeek.MONDAY));
    }

    void dispatchWeeklyReportReminder(LocalDate week) {
        for (GuardianWeeklyReportSubscription subscription : subscriptionService.list()) {
            if (!activeBinding(subscription)
                    || (!Integer.valueOf(1).equals(subscription.getSiteNotificationEnabled())
                    && !Integer.valueOf(1).equals(subscription.getEmailEnabled()))) continue;
            GuardianWeeklyReportResp report = guardianWeeklyReportService.buildWeeklyReport(subscription.getStudentUserId());
            boolean siteDelivered = !Integer.valueOf(1).equals(subscription.getSiteNotificationEnabled())
                    || week.equals(subscription.getSiteLastSentWeek());
            boolean emailDelivered = !Integer.valueOf(1).equals(subscription.getEmailEnabled())
                    || week.equals(subscription.getEmailLastSentWeek());
            if (siteDelivered && emailDelivered) continue;
            boolean deliveryRecorded = false;
            if (!siteDelivered) {
                Msg msg = new Msg(); msg.setMsgType("GUARDIAN_WEEKLY_REPORT"); msg.setMsgTypeText("家长周报");
                msg.setMsgTitle("本周学习汇总已生成"); msg.setMsgContent(summary(report));
                msg.setIsRead(Read.NO); msg.setReceiveId(subscription.getGuardianUserId()); msg.setCreateTime(LocalDateTime.now()); msg.setUpdateTime(LocalDateTime.now());
                siteDelivered = msgService.save(msg);
                if (siteDelivered) {
                    subscription.setSiteLastSentWeek(week);
                    deliveryRecorded = true;
                }
            }
            if (!emailDelivered) {
                emailDelivered = sendEmailIfEnabled(subscription, userService.getById(subscription.getGuardianUserId()), report);
                if (emailDelivered) {
                    subscription.setEmailLastSentWeek(week);
                    deliveryRecorded = true;
                }
            }
            if (siteDelivered && emailDelivered) subscription.setLastSentWeek(week);
            if (deliveryRecorded) {
                subscription.setUpdateTime(LocalDateTime.now());
                subscriptionService.updateById(subscription);
            }
        }
    }
    private boolean activeBinding(GuardianWeeklyReportSubscription subscription) {
        return studentGuardianRelMapper.selectCount(new LambdaQueryWrapper<StudentGuardianRel>().eq(StudentGuardianRel::getGuardianUserId, subscription.getGuardianUserId()).eq(StudentGuardianRel::getStudentUserId, subscription.getStudentUserId()).eq(StudentGuardianRel::getStatus, GuardianBindingStatus.ACTIVE)) > 0;
    }
    private boolean sendEmailIfEnabled(GuardianWeeklyReportSubscription subscription, User guardian, GuardianWeeklyReportResp report) {
        if (!Integer.valueOf(1).equals(subscription.getEmailEnabled()) || guardian == null || guardian.getEmail() == null || guardian.getEmail().trim().isEmpty() || mailConfig.getHost() == null || mailConfig.getHost().trim().isEmpty()) return false;
        return mailSender.sendEmail(new String[]{guardian.getEmail()}, "本周学习汇总", summary(report), mailConfig);
    }
    private String summary(GuardianWeeklyReportResp report) { return report.getStudentName() + "本周新增错题" + report.getNewWrongQuestionCount() + "道，订正" + report.getCorrectedCount() + "道，有效复习" + report.getEffectiveReviewCount() + "次，保持率" + report.getRetentionRate() + "%；逾期" + report.getOverdueCount() + "项。" + report.getNextWeekSuggestion(); }
}
