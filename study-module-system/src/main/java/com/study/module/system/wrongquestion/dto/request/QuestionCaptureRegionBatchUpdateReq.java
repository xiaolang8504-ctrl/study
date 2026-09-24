package com.study.module.system.wrongquestion.dto.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 批量设置当前采集任务中待确认题块的归类信息。
 */
@Data
public class QuestionCaptureRegionBatchUpdateReq {
    @NotNull(message = "采集任务ID不能为空")
    private Long taskId;

    @NotEmpty(message = "请选择题块")
    @Size(max = 200, message = "每次最多设置200个题块")
    private List<@NotNull Long> regionIds;

    private String grade;
    private String subject;
    private String questionType;
    private String source;
    private String learningPoint;
}
