package com.study.module.system.questionbank.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.module.system.questionbank.entity.QuestionBank;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface QuestionBankMapper extends BaseMapper<QuestionBank> {

    /**
     * 结构化条件先收窄候选集，再由 MySQL FULLTEXT 计算文本相关度。
     */
    @Select("<script>SELECT q.*, MATCH(q.question_title,q.question_content) "
            + "AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) AS relevance "
            + "FROM sys_question_bank q WHERE q.review_status=1 AND q.enable=1 AND (q.expire_at IS NULL OR q.expire_at &gt;= CURDATE()) "
            + "AND q.grade=#{grade} AND q.subject=#{subject} "
            + "<if test='questionType != null and questionType != \"\"'>AND q.question_type=#{questionType}</if> "
            + "AND NOT EXISTS (SELECT 1 FROM sys_question_recommendation_log rl WHERE rl.user_id=#{userId} "
            + "AND rl.bank_question_id=q.id AND rl.exposure_time &gt; DATE_SUB(NOW(), INTERVAL 30 DAY)) "
            + "AND MATCH(q.question_title,q.question_content) AGAINST(#{keyword} IN NATURAL LANGUAGE MODE) "
            + "ORDER BY relevance DESC, ABS(q.difficulty-#{difficulty}) ASC, q.id DESC LIMIT #{limit}</script>")
    List<QuestionBank> selectSimilarCandidates(@Param("userId") Long userId,
                                               @Param("grade") String grade,
                                               @Param("subject") String subject,
                                               @Param("questionType") String questionType,
                                               @Param("difficulty") Integer difficulty,
                                               @Param("keyword") String keyword,
                                               @Param("limit") Integer limit);

    /**
     * 知识点重合优先，同一学生30天内已曝光题目不重复推荐。
     */
    @Select("<script>SELECT q.* FROM sys_question_bank q "
            + "JOIN sys_question_knowledge_point qkp ON qkp.question_id=q.id "
            + "WHERE q.review_status=1 AND q.enable=1 AND (q.expire_at IS NULL OR q.expire_at &gt;= CURDATE()) AND q.grade=#{grade} AND q.subject=#{subject} "
            + "<if test='questionType != null and questionType != \"\"'>AND q.question_type=#{questionType}</if> "
            + "AND qkp.knowledge_point_id IN "
            + "<foreach collection='pointIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> "
            + "AND NOT EXISTS (SELECT 1 FROM sys_question_recommendation_log rl WHERE rl.user_id=#{userId} "
            + "AND rl.bank_question_id=q.id AND rl.exposure_time &gt; DATE_SUB(NOW(), INTERVAL 30 DAY)) "
            + "GROUP BY q.id ORDER BY COUNT(DISTINCT qkp.knowledge_point_id) DESC, "
            + "ABS(q.difficulty-#{difficulty}) ASC, q.id DESC LIMIT #{limit}</script>")
    List<QuestionBank> selectKnowledgeCandidates(@Param("userId") Long userId,
                                                 @Param("grade") String grade,
                                                 @Param("subject") String subject,
                                                 @Param("questionType") String questionType,
                                                 @Param("difficulty") Integer difficulty,
                                                 @Param("pointIds") List<Long> pointIds,
                                                 @Param("limit") Integer limit);

    /**
     * 候选不足时按结构化条件降级；excludeRecent控制是否排除近30天曝光。
     */
    @Select("<script>SELECT q.* FROM sys_question_bank q WHERE q.review_status=1 AND q.enable=1 AND (q.expire_at IS NULL OR q.expire_at &gt;= CURDATE()) "
            + "AND q.grade=#{grade} AND q.subject=#{subject} "
            + "<if test='strictType and questionType != null and questionType != \"\"'>AND q.question_type=#{questionType}</if> "
            + "<if test='excludeRecent'>AND NOT EXISTS (SELECT 1 FROM sys_question_recommendation_log rl "
            + "WHERE rl.user_id=#{userId} AND rl.bank_question_id=q.id "
            + "AND rl.exposure_time &gt; DATE_SUB(NOW(), INTERVAL 30 DAY))</if> "
            + "ORDER BY ABS(q.difficulty-#{difficulty}) ASC, q.id DESC LIMIT #{limit}</script>")
    List<QuestionBank> selectFallbackCandidates(@Param("userId") Long userId,
                                                @Param("grade") String grade,
                                                @Param("subject") String subject,
                                                @Param("questionType") String questionType,
                                                @Param("difficulty") Integer difficulty,
                                                @Param("strictType") boolean strictType,
                                                @Param("excludeRecent") boolean excludeRecent,
                                                @Param("limit") Integer limit);
}
