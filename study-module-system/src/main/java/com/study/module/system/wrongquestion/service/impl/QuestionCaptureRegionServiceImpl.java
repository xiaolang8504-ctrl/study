package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.wrongquestion.entity.QuestionCaptureRegion;
import com.study.module.system.wrongquestion.mapper.QuestionCaptureRegionMapper;
import com.study.module.system.wrongquestion.service.QuestionCaptureRegionService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * 题目采集题块实体服务实现
 */
@Service
public class QuestionCaptureRegionServiceImpl extends ServiceImpl<QuestionCaptureRegionMapper, QuestionCaptureRegion>
        implements QuestionCaptureRegionService {

    private static final int REGION_WAITING_CONFIRM = 0;
    private static final int REGION_CONFIRMED = 1;

    /**
     * 将待确认题块更新为已确认，并返回受影响行数。
     */
    @Override
    public int confirmWaitingRegion(Long id, LocalDateTime updateTime) {
        return baseMapper.update(null, new LambdaUpdateWrapper<QuestionCaptureRegion>()
                .eq(QuestionCaptureRegion::getId, id)
                .eq(QuestionCaptureRegion::getStatus, REGION_WAITING_CONFIRM)
                .set(QuestionCaptureRegion::getStatus, REGION_CONFIRMED)
                .set(QuestionCaptureRegion::getUpdateTime, updateTime));
    }
}
