package com.study.module.system.review.service.impl;

import com.study.common.core.enums.ErrorCodeConstants;
import com.study.common.core.exception.LogicException;
import com.study.module.system.msg.constants.Read;
import com.study.module.system.msg.entity.Msg;
import com.study.module.system.msg.service.MsgService;
import com.study.module.system.review.entity.ReviewReminder;
import com.study.module.system.review.service.ReviewReminderService;
import com.study.module.system.review.service.ReviewReminderUpdateService;
import com.yunshang.budget.common.security.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 复习提醒更新服务实现
 */
@Service
public class ReviewReminderUpdateServiceImpl implements ReviewReminderUpdateService {

    @Autowired
    ReviewReminderService reviewReminderService;

    @Autowired
    MsgService msgService;

    /**
     * 标记复习提醒已读
     */
    @Override
    public void readReviewReminder(Long id) {
        Long userId = AccountUtils.getUserId();
        ReviewReminder reminder = reviewReminderService.lambdaQuery()
                .eq(ReviewReminder::getId, id)
                .eq(ReviewReminder::getUserId, userId)
                .one();
        if (reminder == null || reminder.getMsgId() == null) {
            throw new LogicException(ErrorCodeConstants.REVIEW_REMINDER_NOT_EXIST);
        }

        // 同时校验消息接收人，避免通过提醒ID修改其他用户的站内消息。
        boolean updated = msgService.lambdaUpdate()
                .eq(Msg::getId, reminder.getMsgId())
                .eq(Msg::getReceiveId, userId)
                .set(Msg::getIsRead, Read.YES)
                .set(Msg::getUpdateTime, LocalDateTime.now())
                .update();
        if (!updated) {
            throw new LogicException(ErrorCodeConstants.REVIEW_REMINDER_READ_FAIL);
        }
    }
}
