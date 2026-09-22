package com.study.module.system.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.module.system.review.entity.ReviewRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

/**
 * 复习记录数据库访问层
 */
public interface ReviewRecordMapper extends BaseMapper<ReviewRecord> {

    @Select("SELECT COUNT(*) FROM sys_review_record WHERE user_id = #{userId} AND feedback >= 2")
    long countPositiveFeedback(@Param("userId") Long userId);

    @Select("SELECT COALESCE(AVG(answer_duration_seconds), 0) FROM sys_review_record " +
            "WHERE user_id = #{userId}")
    double averageAnswerDuration(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM sys_review_record WHERE user_id = #{userId} " +
            "AND subject = #{subject} AND feedback >= 2")
    long countPositiveFeedbackBySubject(@Param("userId") Long userId,
                                        @Param("subject") String subject);

    @Select("SELECT COALESCE(AVG(answer_duration_seconds), 0) FROM sys_review_record " +
            "WHERE user_id = #{userId} AND subject = #{subject}")
    double averageAnswerDurationBySubject(@Param("userId") Long userId,
                                          @Param("subject") String subject);

    @Select("SELECT COUNT(*) FROM sys_review_record WHERE user_id = #{userId} " +
            "AND is_correct IS NOT NULL")
    /**
     * 统计已判定的作答数量。
     */
    long countJudgedAnswer(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM sys_review_record WHERE user_id = #{userId} AND is_correct = 1")
    /**
     * 统计答对的作答数量。
     */
    long countCorrectAnswer(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM sys_review_record WHERE user_id = #{userId} " +
            "AND subject = #{subject} AND is_correct IS NOT NULL")
    long countJudgedAnswerBySubject(@Param("userId") Long userId,
                                    @Param("subject") String subject);

    @Select("SELECT COUNT(*) FROM sys_review_record WHERE user_id = #{userId} " +
            "AND subject = #{subject} AND is_correct = 1")
    long countCorrectAnswerBySubject(@Param("userId") Long userId,
                                     @Param("subject") String subject);

    /**
     * 统计指定科目当天已完成的复习题数量，同一题重复提交只计一次。
     */
    @Select("SELECT COUNT(DISTINCT rr.review_item_id) FROM sys_review_record rr " +
            "WHERE rr.user_id = #{userId} AND rr.subject = #{subject} " +
            "AND rr.review_time >= #{todayStart} " +
            "AND rr.review_time < #{tomorrowStart}")
    long countTodayCompletedBySubject(@Param("userId") Long userId,
                                      @Param("subject") String subject,
                                      @Param("todayStart") LocalDateTime todayStart,
                                      @Param("tomorrowStart") LocalDateTime tomorrowStart);

    @Select("SELECT COUNT(DISTINCT review_item_id) FROM sys_review_record " +
            "WHERE user_id = #{userId} AND review_time >= #{todayStart} " +
            "AND review_time < #{tomorrowStart}")
    long countTodayCompleted(@Param("userId") Long userId,
                             @Param("todayStart") LocalDateTime todayStart,
                             @Param("tomorrowStart") LocalDateTime tomorrowStart);
}
