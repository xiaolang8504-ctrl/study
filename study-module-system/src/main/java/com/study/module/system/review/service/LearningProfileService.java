package com.study.module.system.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.review.dto.request.UpdateLearningProfileReq;
import com.study.module.system.review.dto.response.LearningProfileResp;
import com.study.module.system.review.entity.LearningProfile;

/**
 * 学习偏好服务。
 */
public interface LearningProfileService extends IService<LearningProfile> {

    /** 查询当前登录学生的学习偏好。 */
    LearningProfileResp learningProfile();

    /** 更新当前登录学生的学习偏好。 */
    void updateLearningProfile(UpdateLearningProfileReq request);
}
