package com.study.module.system.wrongquestion.dto.request;
import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
/**
 * 题目采集结果确认请求
 */
@Data
public class QuestionCaptureConfirmReq {
    @NotNull(message = "采集任务ID不能为空") private Long taskId;
    @NotEmpty(message = "请选择至少一个题块") private List<@NotNull Long> regionIds;
}
