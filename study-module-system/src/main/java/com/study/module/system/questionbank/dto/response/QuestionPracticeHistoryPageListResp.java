package com.study.module.system.questionbank.dto.response;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
/**
 * 相似题练习历史分页响应。
 */
@Data
public class QuestionPracticeHistoryPageListResp {
    private Long id;
    private String batchNo;
    private Long wrongQuestionId;
    private String wrongQuestionTitle;
    private Long bankQuestionId;
    private String questionTitle;
    private Double recommendScore;
    private String matchType;
    private String experimentGroup;
    private String studentAnswer;
    private Integer isCorrect;
    private String judgeType;
    private Integer durationSeconds;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime exposureTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime answerTime;
}
