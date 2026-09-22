package com.study.module.system.wrongquestion.dto.response;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
/**
 * 题目采集任务详情响应
 */
@Data
public class QuestionCaptureTaskResp {
    private Long id; private Integer status; private String failReason; private Integer retryCount; private String grade; private String subject; private String questionType; private String source; private LocalDateTime createTime;
    @ApiModelProperty("任务总页数") private Integer totalPageCount;
    @ApiModelProperty("已完成识别页数") private Integer completedPageCount;
    @ApiModelProperty("识别失败页数") private Integer failedPageCount;
    @ApiModelProperty("待处理或识别中页数") private Integer processingPageCount;
    @ApiModelProperty("待确认题块") private List<QuestionCaptureRegionResp> regionList;
    private List<QuestionCapturePageResp> pageList;
}
