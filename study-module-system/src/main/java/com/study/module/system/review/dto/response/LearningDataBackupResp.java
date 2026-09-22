package com.study.module.system.review.dto.response;

import com.study.module.system.review.entity.PracticeSession;
import com.study.module.system.review.entity.PracticeSessionQuestion;
import com.study.module.system.review.entity.ReviewItem;
import com.study.module.system.review.entity.ReviewPlan;
import com.study.module.system.review.entity.ReviewRecord;
import com.study.module.system.review.entity.ReviewSubjectSetting;
import com.study.module.system.wrongquestion.entity.WrongQuestion;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 当前学生个人学习数据备份响应。
 */
@Data
public class LearningDataBackupResp {

    @ApiModelProperty("备份格式版本")
    private String backupVersion;

    @ApiModelProperty("生成时间")
    private LocalDateTime generatedTime;

    @ApiModelProperty("学习偏好")
    private LearningProfileResp learningProfile;

    @ApiModelProperty("复习计划")
    private ReviewPlan reviewPlan;

    @ApiModelProperty("复习科目设置")
    private List<ReviewSubjectSetting> reviewSubjectSettingList;

    @ApiModelProperty("错题数据")
    private List<WrongQuestion> wrongQuestionList;

    @ApiModelProperty("复习排期数据")
    private List<ReviewItem> reviewItemList;

    @ApiModelProperty("复习记录")
    private List<ReviewRecord> reviewRecordList;

    @ApiModelProperty("个人组卷记录")
    private List<PracticeSession> practiceSessionList;

    @ApiModelProperty("个人组卷题目记录")
    private List<PracticeSessionQuestion> practiceSessionQuestionList;
}
