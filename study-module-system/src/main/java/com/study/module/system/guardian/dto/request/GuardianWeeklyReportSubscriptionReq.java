package com.study.module.system.guardian.dto.request;
import lombok.Data;
import javax.validation.constraints.NotNull;
@Data public class GuardianWeeklyReportSubscriptionReq {
    @NotNull(message = "学生用户ID不能为空") private Long studentUserId;
    @NotNull(message = "站内提醒开关不能为空") private Integer siteNotificationEnabled;
    @NotNull(message = "邮件提醒开关不能为空") private Integer emailEnabled;
}
