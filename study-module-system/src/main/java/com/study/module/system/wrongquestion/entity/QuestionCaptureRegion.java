package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * OCR 自动切出的、等待学生确认的题块。
 */
@Data
public class QuestionCaptureRegion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long pageId;
    private Integer regionNo;
    private Integer confidence;
    /** OCR 字段级置信度，缺少字段时为0。 */
    private Integer questionTitleConfidence;
    private Integer questionContentConfidence;
    private Integer wrongAnswerConfidence;
    private Integer correctAnswerConfidence;
    private Integer analysisConfidence;
    /**
     * 用户保存过文本或边框后标识为人工校正。
     */
    private Integer manuallyCorrected;
    private Long wrongQuestionId;
    /**
     * 归一化边框坐标，取值0-10000。
     */
    private Integer leftPosition;
    private Integer topPosition;
    private Integer width;
    private Integer height;
    /**
     * 0待确认、1已确认、2跳过
     */
    private Integer status;
    private String questionTitle;
    private String questionContent;
    private String wrongAnswer;
    private String correctAnswer;
    private String wrongReason;
    private String analysis;
    /**
     * 下列字段允许按题块覆盖任务默认值，处理混合试卷时以题块值优先。
     */
    private String grade;
    private String subject;
    private String questionType;
    private String source;
    private String learningPoint;
    private String errorLabels;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
