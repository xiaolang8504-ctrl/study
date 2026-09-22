package com.study.module.schedule.stock;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 库存快照定时处理器
 */
@Slf4j
@Component
public class StockSnapshotSchedule {

    /**
     * 创建库存快照
     */
    @Scheduled(cron="${cron.stockCreateSnapshot:-}")
    public void createSnapshot() {
        log.info("【创建库存快照】开始执行任务");
        //stockProvider.createSnapshot();
        log.info("【创建库存快照】任务执行完成");
    }
}
