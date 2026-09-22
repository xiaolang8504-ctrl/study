package com.study.module.system.wrongquestion.dto.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 采集题块快照项。
 */
@Data
public class QuestionCaptureRegionSnapshotItemReq {
    @NotNull(message = "题块ID不能为空") private Long id;
    @NotNull(message = "页面ID不能为空") private Long pageId;
    @NotNull(message = "题块编号不能为空") private Integer regionNo;
    @NotNull(message = "题块状态不能为空") private Integer status;
    private Integer confidence; private Integer questionTitleConfidence; private Integer questionContentConfidence; private Integer wrongAnswerConfidence; private Integer correctAnswerConfidence; private Integer analysisConfidence; private Integer manuallyCorrected;
    @NotNull(message = "题块左坐标不能为空") private Integer leftPosition;
    @NotNull(message = "题块上坐标不能为空") private Integer topPosition;
    @NotNull(message = "题块宽度不能为空") private Integer width;
    @NotNull(message = "题块高度不能为空") private Integer height;
    private String questionTitle; private String questionContent; private String wrongAnswer; private String correctAnswer; private String wrongReason; private String analysis;
    private String grade; private String subject; private String questionType; private String source; private String learningPoint; private String errorLabels;
}
