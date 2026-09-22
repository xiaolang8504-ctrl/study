package com.study.module.system.wrongquestion.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionKnowledgePointStatisticsResp;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 初中生错题归档数据库访问层
 */
public interface WrongQuestionMapper extends BaseMapper<WrongQuestion> {

    /**
     * 按知识点聚合当前用户的错题状态数量。
     */
    @Select("<script>"
            + "SELECT r.knowledge_point_id AS knowledgePointId, "
            + "kp.point_name AS knowledgePointName, kp.grade, kp.subject, "
            + "COUNT(*) AS wrongQuestionCount, "
            + "SUM(CASE WHEN wq.status = 0 THEN 1 ELSE 0 END) AS pendingCorrectionCount, "
            + "SUM(CASE WHEN wq.status = 1 THEN 1 ELSE 0 END) AS correctedCount, "
            + "SUM(CASE WHEN wq.status = 2 THEN 1 ELSE 0 END) AS masteredCount, "
            + "SUM(CASE WHEN wq.status = 3 THEN 1 ELSE 0 END) AS archivedCount "
            + "FROM sys_wrong_question wq "
            + "INNER JOIN sys_wrong_question_knowledge_point r ON r.wrong_question_id = wq.id "
            + "LEFT JOIN sys_knowledge_point kp ON kp.id = r.knowledge_point_id "
            + "WHERE wq.create_id = #{userId} "
            + "<if test='grade != null and grade != \"\"'> AND wq.grade = #{grade} </if> "
            + "<if test='subject != null and subject != \"\"'> AND wq.subject = #{subject} </if> "
            + "<if test='status != null'> AND wq.status = #{status} </if> "
            + "GROUP BY r.knowledge_point_id, kp.point_name, kp.grade, kp.subject "
            + "ORDER BY wrongQuestionCount DESC, r.knowledge_point_id"
            + "</script>")
    List<WrongQuestionKnowledgePointStatisticsResp> selectKnowledgePointStatistics(
            @Param("userId") Long userId,
            @Param("grade") String grade,
            @Param("subject") String subject,
            @Param("status") Integer status);
}
