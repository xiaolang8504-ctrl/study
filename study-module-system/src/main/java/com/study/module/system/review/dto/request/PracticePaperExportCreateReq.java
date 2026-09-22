package com.study.module.system.review.dto.request;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
@Data public class PracticePaperExportCreateReq {
    @NotNull(message = "练习会话ID不能为空") private Long sessionId;
    @NotBlank(message = "导出格式不能为空") private String format;
    @NotNull(message = "答案版标识不能为空") private Integer answerMode;
}
