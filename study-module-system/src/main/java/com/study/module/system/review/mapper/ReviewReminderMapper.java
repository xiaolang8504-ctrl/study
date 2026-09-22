package com.study.module.system.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.module.system.review.entity.ReviewReminder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 复习提醒数据库访问层
 */
public interface ReviewReminderMapper extends BaseMapper<ReviewReminder> {

    /**
     * 按用户、日期和类型幂等创建提醒任务。
     */
    @Insert("INSERT IGNORE INTO sys_review_reminder " +
            "(plan_id, user_id, reminder_date, reminder_type, due_count, overdue_count, subject_summary, " +
            "scheduled_time, send_status, retry_count, failure_reason, create_time, update_time) " +
            "VALUES (#{reminder.planId}, #{reminder.userId}, #{reminder.reminderDate}, " +
            "#{reminder.reminderType}, #{reminder.dueCount}, #{reminder.overdueCount}, " +
            "#{reminder.subjectSummary}, " +
            "#{reminder.scheduledTime}, #{reminder.sendStatus}, #{reminder.retryCount}, " +
            "#{reminder.failureReason}, #{reminder.createTime}, #{reminder.updateTime})")
    /**
     * 插入复习提醒；重复时忽略。
     */
    int insertIgnoreReviewReminder(@Param("reminder") ReviewReminder reminder);

    /**
     * 原子累计失败次数并保存最近一次失败原因。
     */
    @Update("UPDATE sys_review_reminder SET send_status = 2, retry_count = retry_count + 1, " +
            "failure_reason = #{failureReason}, update_time = #{updateTime} " +
            "WHERE user_id = #{userId} AND reminder_date = #{reminderDate} " +
            "AND reminder_type = #{reminderType} AND send_status <> 1")
    int markReviewReminderFailed(@Param("userId") Long userId,
                                 @Param("reminderDate") LocalDate reminderDate,
                                 @Param("reminderType") String reminderType,
                                 @Param("failureReason") String failureReason,
                                 @Param("updateTime") LocalDateTime updateTime);
}
