package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 采集任务中的原始页面。
 */
@Data
public class QuestionCapturePage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long taskId;
    private Long imageFileId;
    /** 真正去除彩色笔迹后的页面文件；永不覆盖 imageFileId。 */
    private Long cleanedFileId;
    /** 仅供预览的灰度文件，不是去笔迹结果。 */
    private Long grayscaleFileId;
    private Long sourceFileId;
    private Integer sourcePageNo;
    private Integer pageNo;
    private Integer status;
    private String failReason;
    private Integer retryCount;
    /**
     * 去笔迹状态：0未生成、1生成中、2已完成、4失败。
     */
    private Integer cleanStatus;
    private String cleanFailReason;
    private String cleanProvider;
    private String cleanAlgorithmVersion;
    private Integer cleanQualityScore;
    private LocalDateTime cleanRequestedTime;
    private LocalDateTime cleanFinishedTime;
    /** 灰度预览状态：0未生成、1生成中、2已完成、4失败。 */
    private Integer grayscaleStatus;
    private String grayscaleFailReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
