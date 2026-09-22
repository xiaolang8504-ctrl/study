package com.study.module.system.wrongquestion.dto.response;
import lombok.Data;
/**
 * 题目采集题块响应
 */
@Data
public class QuestionCaptureRegionResp {
    private Long id; private Long pageId; private Integer regionNo; private Integer confidence; private Integer questionTitleConfidence; private Integer questionContentConfidence; private Integer wrongAnswerConfidence; private Integer correctAnswerConfidence; private Integer analysisConfidence; private Integer status; private Integer manuallyCorrected; private Long wrongQuestionId;
    private Integer leftPosition; private Integer topPosition; private Integer width; private Integer height;
    private String questionTitle; private String questionContent; private String wrongAnswer; private String correctAnswer; private String wrongReason; private String analysis;
    private String grade; private String subject; private String questionType; private String source; private String learningPoint; private String errorLabels;
}
