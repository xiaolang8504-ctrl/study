package com.study.module.system.review.service;

import com.study.module.system.review.dto.request.UpdateReviewPlanSettingReq;
import com.study.module.system.review.dto.response.ReviewPlanSettingResp;

/**
 * 复习计划设置服务
 */
public interface ReviewPlanSettingService {

    /**
     * 查询当前用户的复习计划设置
     */
    ReviewPlanSettingResp reviewPlanSetting();

    /**
     * 更新当前用户的复习计划设置
     */
    void updateReviewPlanSetting(UpdateReviewPlanSettingReq request);
}
