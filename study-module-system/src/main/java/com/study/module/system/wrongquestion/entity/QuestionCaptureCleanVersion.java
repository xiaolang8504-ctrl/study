package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/** 采集页去笔迹处理的可回退版本记录。 */
@Data
public class QuestionCaptureCleanVersion {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long pageId;
    private Long taskId;
    private Long userId;
    private Long originalFileId;
    private Long cleanedFileId;
    private String provider;
    private String algorithmVersion;
    private Integer status;
    private Integer qualityScore;
    private String failReason;
    private Integer activeFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
