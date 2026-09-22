package com.study.module.system.wrongquestion.dto.request;
import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
/**
 * 题目采集题块批量操作请求
 */
@Data
public class QuestionCaptureRegionIdsReq { @NotNull(message="采集任务ID不能为空") private Long taskId; @NotEmpty(message="请选择题块") private List<Long> regionIds; }
