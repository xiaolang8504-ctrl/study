package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 初中生错题归档实体
 */
@Data
public class WrongQuestion {

    /**
     * 错题ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 年级字典键值
     */
    private String grade;

    /**
     * 年级名称
     */
    private String gradeName;

    /**
     * 科目字典键值
     */
    private String subject;

    /**
     * 科目名称
     */
    private String subjectName;

    /**
     * 题目类型字典键值
     */
    private String questionType;

    /**
     * 题目类型名称
     */
    private String questionTypeName;

    /**
     * 题目标题
     */
    private String questionTitle;

    /**
     * 题目内容
     */
    private String questionContent;

    /** 内容格式：TEXT、LATEX、RICH_TEXT。 */
    private String contentFormat;

    /** 结构化选项JSON。 */
    private String optionsJson;

    /** 规范化题干指纹。 */
    private String questionFingerprint;

    /** 合并后的主错题ID。 */
    private Long mergedToId;

    /**
     * 错误答案
     */
    private String wrongAnswer;

    /**
     * 正确答案
     */
    private String correctAnswer;

    /**
     * 错误原因
     */
    private String wrongReason;

    /**
     * 题目解析
     */
    private String analysis;

    /**
     * 解题关键提示。仅给出思考方向，不直接给出结果。
     */
    private String keyHint;

    /**
     * 分步解题过程。
     */
    private String solutionSteps;

    /**
     * 本题常见易错点。
     */
    private String commonMistake;

    /**
     * 默认题目图片地址，选择题时为A选项图片地址
     */
    private String imageUrl;

    /**
     * B选项图片地址
     */
    private String imageUrl2;

    /**
     * C选项图片地址
     */
    private String imageUrl3;

    /**
     * D选项图片地址
     */
    private String imageUrl4;

    /**
     * 图片采集来源定位信息，用于在错题详情回看原试卷中的题块。
     */
    private Long captureTaskId;
    private Long capturePageId;
    private Long captureRegionId;
    private Integer captureSourcePageNo;
    private Integer captureLeftPosition;
    private Integer captureTopPosition;
    private Integer captureWidth;
    private Integer captureHeight;
    /** 采集页面原图文件ID，用于原图与清理图始终可追溯。 */
    private Long captureOriginalFileId;
    /** 确认时可用的清理页面文件ID。 */
    private Long captureCleanedFileId;
    /** 题块裁剪来源：ORIGINAL、CLEANED。 */
    private String captureImageMode;

    /** 教材版本，例如人教版、北师大版。 */
    private String textbookVersion;

    /** 教材章节名称，暂以学生可编辑文本保存。 */
    private String chapterName;

    /** 是否收藏：0否，1是。 */
    private Integer favorite;

    /** 学生整理优先级：0普通，1-5逐步提高。 */
    private Integer priorityLevel;

    /**
     * 知识点
     */
    private String learningPoint;

    /**
     * 错误类型标签
     */
    private String errorLabels;

    /**
     * 结构化错因编码：READING、CONCEPT、METHOD、CALCULATION、EXPRESSION，多个用逗号分隔。
     */
    private String errorCauseCodes;

    /**
     * 能力层级：FOUNDATION基础、APPLICATION应用、COMPREHENSIVE综合。
     */
    private String abilityLevel;

    /**
     * 错题难度等级，数值越大优先级越高
     */
    private Integer level;

    /**
     * 来源字典键值: 1考试, 2测验, 3日常练习, 4自学
     */
    private String source;

    /**
     * 来源名称
     */
    private String sourceName;

    /**
     * 状态: 0待改, 1已改, 2已掌握, 3已归档
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long createId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
