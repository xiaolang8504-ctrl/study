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
    private Long cleanedFileId;
    private Long sourceFileId;
    private Integer sourcePageNo;
    private Integer pageNo;
    private Integer status;
    private String failReason;
    private Integer retryCount;
    /**
     * 0未生成、1生成中、2已完成、4失败。
     */
    private Integer cleanStatus;
    private String cleanFailReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
