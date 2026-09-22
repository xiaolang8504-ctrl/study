package com.study.module.system.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.module.system.review.entity.ReviewSubjectSetting;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 复习计划科目设置数据库访问层
 */
public interface ReviewSubjectSettingMapper extends BaseMapper<ReviewSubjectSetting> {

    /**
     * 新增或更新科目设置
     */
    @Insert("INSERT INTO sys_review_subject_setting " +
            "(plan_id, user_id, subject, subject_name, enabled, daily_limit, create_time, update_time) " +
            "VALUES (#{setting.planId}, #{setting.userId}, #{setting.subject}, #{setting.subjectName}, " +
            "#{setting.enabled}, #{setting.dailyLimit}, #{setting.createTime}, #{setting.updateTime}) " +
            "ON DUPLICATE KEY UPDATE subject_name = VALUES(subject_name), enabled = VALUES(enabled), " +
            "daily_limit = VALUES(daily_limit), update_time = VALUES(update_time)")
    int insertOrUpdateReviewSubjectSetting(@Param("setting") ReviewSubjectSetting setting);
}
