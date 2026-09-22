package com.study.module.system.review.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.module.system.msg.constants.Read;
import com.study.module.system.msg.entity.Msg;
import com.study.module.system.msg.service.MsgService;
import com.study.module.system.review.constants.ReviewReminderStatus;
import com.study.module.system.review.dto.request.ReviewReminderPageListReq;
import com.study.module.system.review.dto.response.ReviewReminderHomeResp;
import com.study.module.system.review.dto.response.ReviewReminderPageListResp;
import com.study.module.system.review.entity.ReviewReminder;
import com.study.module.system.review.service.ReviewReminderListService;
import com.study.module.system.review.service.ReviewReminderService;
import com.yunshang.budget.common.mybatis.utils.PageUtils;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 复习提醒列表服务实现
 */
@Service
public class ReviewReminderListServiceImpl implements ReviewReminderListService {

    @Autowired
    ReviewReminderService reviewReminderService;

    @Autowired
    MsgService msgService;

    /**
     * 分页查询复习提醒
     */
    @Override
    public ReviewReminderHomeResp reviewReminderPageList(ReviewReminderPageListReq request) {
        Long userId = AccountUtils.getUserId();
        Page<ReviewReminder> page = new Page<>(request.getCurrent(), request.getPageSize());
        reviewReminderService.lambdaQuery()
                .eq(ReviewReminder::getUserId, userId)
                .eq(ReviewReminder::getSendStatus, ReviewReminderStatus.SENT)
                .orderByDesc(ReviewReminder::getSentTime)
                .page(page);

        List<Long> msgIds = page.getRecords().stream()
                .map(ReviewReminder::getMsgId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        Map<Long, Msg> msgMap = msgIds.isEmpty() ? Collections.emptyMap() : msgService.listByIds(msgIds)
                .stream().filter(msg -> userId.equals(msg.getReceiveId()))
                .collect(Collectors.toMap(Msg::getId, Function.identity()));

        ReviewReminderHomeResp response = new ReviewReminderHomeResp();
        response.setUnreadCount(msgService.lambdaQuery()
                .eq(Msg::getReceiveId, userId)
                .eq(Msg::getMsgType, "dailyReview")
                .eq(Msg::getIsRead, Read.NO)
                .count());
        response.setPageResult(PageUtils.wrap(page, reminders -> reminders.stream()
                .map(reminder -> buildPageResponse(reminder, msgMap.get(reminder.getMsgId())))
                .collect(Collectors.toList())));
        return response;
    }

    /**
     * 构建分页响应
     */
    private ReviewReminderPageListResp buildPageResponse(ReviewReminder reminder, Msg msg) {
        ReviewReminderPageListResp response = new ReviewReminderPageListResp();
        response.setId(reminder.getId());
        response.setMsgId(reminder.getMsgId());
        response.setTitle(msg == null ? "复习提醒" : msg.getMsgTitle());
        response.setContent(msg == null ? "" : msg.getMsgContent());
        response.setDueCount(reminder.getDueCount());
        response.setOverdueCount(reminder.getOverdueCount());
        response.setSubjectSummary(reminder.getSubjectSummary());
        response.setIsRead(msg == null ? Read.YES : msg.getIsRead());
        response.setSentTime(reminder.getSentTime());
        return response;
    }
}
