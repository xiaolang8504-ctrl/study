package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.entity.QuestionCaptureRegion;

import java.time.LocalDateTime;

/**
 * 题目采集题块实体服务
 */
public interface QuestionCaptureRegionService extends IService<QuestionCaptureRegion> {

    /**
     * 将待确认题块原子地标记为已确认。
     */
    int confirmWaitingRegion(Long id, LocalDateTime updateTime);
}
