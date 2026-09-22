package com.study.module.system.review.service;

import com.study.module.system.review.dto.request.ReviewReminderPageListReq;
import com.study.module.system.review.dto.response.ReviewReminderHomeResp;

/**
 * 复习提醒列表服务
 */
public interface ReviewReminderListService {

    /**
     * 分页查询复习提醒
     */
    ReviewReminderHomeResp reviewReminderPageList(ReviewReminderPageListReq request);
}
