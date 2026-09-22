package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionTimelineResp;
import com.study.module.system.wrongquestion.entity.WrongQuestionTimeline;

import java.util.List;

/**
 * 错题学习证据时间线服务。
 */
public interface WrongQuestionTimelineService extends IService<WrongQuestionTimeline> {

    /**
     * 记录一条不可变的学习动作证据。
     */
    void record(Long wrongQuestionId, String eventType, String eventSource, String eventContent, Long operatorId);

    /**
     * 按时间顺序查询错题学习证据。
     */
    List<WrongQuestionTimelineResp> timelineList(Long wrongQuestionId);
}
