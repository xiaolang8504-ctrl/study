package com.study.module.system.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/** 服务端练习卷导出任务与下载历史。 */
@Data
@TableName("sys_practice_paper_export_task")
public class PracticePaperExportTask {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId; private Long sessionId; private String format; private Integer answerMode;
    /** 0排队，1生成中，2成功，3失败。 */
    private Integer status; private Integer fileId; private String fileName; private String errorMessage;
    private LocalDateTime finishTime; private LocalDateTime createTime; private LocalDateTime updateTime;
}
