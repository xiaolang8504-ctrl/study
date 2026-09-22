package com.study.module.system.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.module.system.review.dto.response.ReviewTodayTaskResp;
import com.study.module.system.review.entity.ReviewItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 复习项目数据库访问层
 */
public interface ReviewItemMapper extends BaseMapper<ReviewItem> {

    /**
     * 幂等新增错题复习排期
     */
    @Insert("INSERT IGNORE INTO sys_review_item " +
            "(plan_id, user_id, wrong_question_id, item_status, stage, current_interval_minutes, " +
            "last_feedback, mastery_score, correct_streak, wrong_streak, lapse_count, " +
            "review_count, last_review_time, next_review_time, " +
            "mastered_time, algorithm_version, version, create_time, update_time) " +
            "VALUES (#{item.planId}, #{item.userId}, #{item.wrongQuestionId}, #{item.itemStatus}, " +
            "#{item.stage}, #{item.currentIntervalMinutes}, #{item.lastFeedback}, #{item.masteryScore}, " +
            "#{item.correctStreak}, #{item.wrongStreak}, #{item.lapseCount}, " +
            "#{item.reviewCount}, #{item.lastReviewTime}, #{item.nextReviewTime}, " +
            "#{item.masteredTime}, #{item.algorithmVersion}, #{item.version}, #{item.createTime}, " +
            "#{item.updateTime})")
    /**
     * 插入复习项；重复时忽略。
     */
    int insertIgnoreReviewItem(@Param("item") ReviewItem item);

    /**
     * 查询当前用户今日待复习题单
     */
    @Select("SELECT ri.id AS reviewItemId, ri.wrong_question_id AS wrongQuestionId, " +
            "wq.grade_name AS gradeName, wq.subject_name AS subjectName, " +
            "wq.question_type_name AS questionTypeName, wq.question_title AS questionTitle, " +
            "wq.question_content AS questionContent, wq.image_url AS imageUrl, " +
            "wq.image_url2 AS imageUrl2, wq.image_url3 AS imageUrl3, wq.image_url4 AS imageUrl4, " +
            "wq.learning_point AS learningPoint, wq.error_labels AS errorLabels, " +
            "COALESCE(wq.level, 0) AS level, ri.stage, COALESCE(ri.mastery_score, 0) AS masteryScore, " +
            "ri.correct_streak AS correctStreak, ri.wrong_streak AS wrongStreak, ri.lapse_count AS lapseCount, " +
            "ri.review_count AS reviewCount, ri.next_review_time AS nextReviewTime, " +
            "CASE WHEN ri.next_review_time < #{todayStart} THEN 1 ELSE 0 END AS overdue " +
            "FROM sys_review_item ri " +
            "INNER JOIN sys_wrong_question wq ON wq.id = ri.wrong_question_id " +
            "WHERE ri.user_id = #{userId} AND wq.create_id = #{userId} " +
            "AND wq.subject = #{subject} " +
            "AND ri.item_status = 0 AND ri.next_review_time < #{tomorrowStart} " +
            "AND (ri.last_review_time IS NULL OR ri.last_review_time < #{todayStart} " +
            "OR ri.next_review_time <= #{currentTime}) " +
            "ORDER BY overdue DESC, COALESCE(ri.mastery_score, 0) ASC, COALESCE(wq.level, 0) DESC, " +
            "ri.wrong_streak DESC, ri.lapse_count DESC, ri.next_review_time ASC LIMIT #{limit}")
    List<ReviewTodayTaskResp> todayReviewTaskList(@Param("userId") Long userId,
                                                  @Param("subject") String subject,
                                                  @Param("todayStart") LocalDateTime todayStart,
                                                  @Param("tomorrowStart") LocalDateTime tomorrowStart,
                                                  @Param("currentTime") LocalDateTime currentTime,
                                                  @Param("limit") Integer limit);

    /**
     * 使用版本号更新复习排期
     */
    @Update("UPDATE sys_review_item SET stage = #{item.stage}, " +
            "current_interval_minutes = #{item.currentIntervalMinutes}, " +
            "last_feedback = #{item.lastFeedback}, mastery_score = #{item.masteryScore}, " +
            "correct_streak = #{item.correctStreak}, wrong_streak = #{item.wrongStreak}, " +
            "lapse_count = #{item.lapseCount}, review_count = #{item.reviewCount}, " +
            "last_review_time = #{item.lastReviewTime}, next_review_time = #{item.nextReviewTime}, " +
            "mastered_time = #{item.masteredTime}, algorithm_version = #{item.algorithmVersion}, " +
            "version = version + 1, update_time = #{item.updateTime} " +
            "WHERE id = #{item.id} AND user_id = #{item.userId} AND version = #{expectedVersion}")
    int updateReviewFeedback(@Param("item") ReviewItem item,
                             @Param("expectedVersion") Integer expectedVersion);
}
