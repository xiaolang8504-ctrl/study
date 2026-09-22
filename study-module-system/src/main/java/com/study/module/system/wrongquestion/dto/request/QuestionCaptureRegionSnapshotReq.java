package com.study.module.system.wrongquestion.dto.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 采集工作台撤销或重做时恢复的题块快照。
 */
@Data
public class QuestionCaptureRegionSnapshotReq {

    @NotNull(message = "采集任务ID不能为空")
    private Long taskId;

    @NotEmpty(message = "题块快照不能为空")
    @Valid
    private List<QuestionCaptureRegionSnapshotItemReq> regionList;
}
