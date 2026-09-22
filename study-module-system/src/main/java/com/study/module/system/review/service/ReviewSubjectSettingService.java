package com.study.module.system.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.review.entity.ReviewSubjectSetting;
import com.study.module.system.review.entity.ReviewPlan;

import java.util.List;

/**
 * 复习计划科目设置公共服务
 */
public interface ReviewSubjectSettingService extends IService<ReviewSubjectSetting> {

    /**
     * 获取计划当前生效的全部科目设置，未保存的科目使用默认设置。
     */
    List<ReviewSubjectSetting> effectiveSubjectSettings(ReviewPlan reviewPlan);

    /**
     * 判断科目是否参与当前计划。
     */
    boolean isSubjectEnabled(ReviewPlan reviewPlan, String subject);
}
