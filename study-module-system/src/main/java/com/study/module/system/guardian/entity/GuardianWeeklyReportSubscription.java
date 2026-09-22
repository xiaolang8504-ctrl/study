package com.study.module.system.guardian.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 家长按学生配置的周报订阅偏好。 */
@Data
@TableName("guardian_weekly_report_subscription")
public class GuardianWeeklyReportSubscription {
    @TableId(type = IdType.AUTO) private Long id;
    private Long guardianUserId; private Long studentUserId;
    private Integer siteNotificationEnabled; private Integer emailEnabled;
    /** 兼容早期订阅记录的总发送周次，新逻辑按通道记录送达。 */
    private LocalDate lastSentWeek;
    private LocalDate siteLastSentWeek;
    private LocalDate emailLastSentWeek;
    private LocalDateTime createTime; private LocalDateTime updateTime;
}
