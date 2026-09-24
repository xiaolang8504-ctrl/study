package com.study.module.system.review.dto.response;
import lombok.Data;
import java.time.LocalDateTime;
@Data public class PracticePaperExportTaskResp {
    private Long id; private Long sessionId; private Integer paperVersion; private String format; private Integer answerMode; private Integer status;
    private Integer fileId; private String fileName; private String errorMessage; private LocalDateTime finishTime; private LocalDateTime createTime;
}
