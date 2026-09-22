package com.study.module.system.wrongquestion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionTimelineResp;
import com.study.module.system.wrongquestion.entity.WrongQuestionTimeline;
import com.study.module.system.wrongquestion.mapper.WrongQuestionTimelineMapper;
import com.study.module.system.wrongquestion.service.WrongQuestionTimelineService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 错题学习证据时间线服务实现。
 */
@Service
public class WrongQuestionTimelineServiceImpl extends ServiceImpl<WrongQuestionTimelineMapper, WrongQuestionTimeline>
        implements WrongQuestionTimelineService {

    private static final int CONTENT_MAX_LENGTH = 500;

    @Override
    public void record(Long wrongQuestionId, String eventType, String eventSource, String eventContent, Long operatorId) {
        if (wrongQuestionId == null) {
            return;
        }
        WrongQuestionTimeline timeline = new WrongQuestionTimeline();
        timeline.setWrongQuestionId(wrongQuestionId);
        timeline.setEventType(eventType);
        timeline.setEventSource(eventSource);
        timeline.setEventContent(abbreviate(eventContent));
        timeline.setCreateId(operatorId);
        timeline.setCreateTime(LocalDateTime.now());
        save(timeline);
    }

    @Override
    public List<WrongQuestionTimelineResp> timelineList(Long wrongQuestionId) {
        return list(new LambdaQueryWrapper<WrongQuestionTimeline>()
                        .eq(WrongQuestionTimeline::getWrongQuestionId, wrongQuestionId)
                        .orderByAsc(WrongQuestionTimeline::getCreateTime, WrongQuestionTimeline::getId))
                .stream().map(item -> {
                    WrongQuestionTimelineResp response = new WrongQuestionTimelineResp();
                    BeanUtils.copyProperties(item, response);
                    return response;
                }).collect(Collectors.toList());
    }

    private String abbreviate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= CONTENT_MAX_LENGTH ? value : value.substring(0, CONTENT_MAX_LENGTH);
    }
}
