package com.study.module.system.wrongquestion.dto.request;
import lombok.Data;
import javax.validation.constraints.NotNull;
/**
 * 题目采集题块更新请求
 */
@Data
public class QuestionCaptureRegionUpdateReq {
    @NotNull(message = "题块ID不能为空") private Long id;
    private String questionTitle; private String questionContent; private String wrongAnswer; private String correctAnswer; private String wrongReason; private String analysis;
    private String grade; private String subject; private String questionType; private String source; private String learningPoint; private String errorLabels;
    private Integer leftPosition; private Integer topPosition; private Integer width; private Integer height;
}
