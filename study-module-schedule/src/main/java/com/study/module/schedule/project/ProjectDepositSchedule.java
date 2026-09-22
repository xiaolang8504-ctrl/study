package com.study.module.schedule.project;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 项目保证金定时处理器
 */
@Slf4j
@Component
public class ProjectDepositSchedule {

    /**
     * 缴费提醒
     */
    @Scheduled(cron="${cron.projectDepositReminder:-}")
    public void paymentReminder() {
        log.info("【创建库存快照】开始执行任务");
        //projectProvider.paymentReminder();
        log.info("【创建库存快照】任务执行完成");
    }
}
