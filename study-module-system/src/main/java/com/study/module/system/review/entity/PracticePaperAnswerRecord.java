package com.study.module.system.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 纸面练习卷逐题回填记录实体。
 */
@Data
@TableName("sys_practice_paper_answer_record")
public class PracticePaperAnswerRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 练习会话ID。 */
    private Long sessionId;

    /** 练习卷版本。 */
    private Integer paperVersion;

    /** 练习题目ID。 */
    private Long sessionQuestionId;

    /** 学生ID。 */
    private Long userId;

    /** 当前练习卷的第几次纸面作答。 */
    private Integer attemptNo;

    /** 错题ID；题库题答错后会沉淀为个人错题。 */
    private Long wrongQuestionId;

    /** CORRECT、WRONG、UNANSWERED。 */
    private String answerStatus;

    /** 学生填写的纸面答案或解题步骤。 */
    private String studentAnswer;

    /** 错因说明。 */
    private String errorReason;

    /** 作答用时，秒。 */
    private Integer durationSeconds;

    /** 做完的纸面练习卷附件。 */
    private Long answerFileId;

    /** 学生实际纸面作答时间。 */
    private LocalDateTime answerTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
