package com.study.module.system.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.module.system.review.entity.ReviewPlan;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

/**
 * 复习计划数据库访问层
 */
public interface ReviewPlanMapper extends BaseMapper<ReviewPlan> {

    /**
     * 幂等新增用户复习计划
     */
    @Insert("INSERT IGNORE INTO sys_review_plan " +
            "(user_id, plan_name, daily_limit, reminder_enabled, reminder_time, review_week_days, " +
            "algorithm_version, status, create_time, update_time) " +
            "VALUES (#{plan.userId}, #{plan.planName}, #{plan.dailyLimit}, #{plan.reminderEnabled}, " +
            "#{plan.reminderTime}, #{plan.reviewWeekDays}, #{plan.algorithmVersion}, #{plan.status}, " +
            "#{plan.createTime}, #{plan.updateTime})")
    int insertIgnoreReviewPlan(@Param("plan") ReviewPlan plan);
}
