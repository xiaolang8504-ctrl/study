package com.study.module.system.file.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 题目孤立图片清理响应
 */
@Data
public class QuestionImageCleanResp {

    @ApiModelProperty("请求清理数量")
    private Integer requestedCount;

    @ApiModelProperty("实际删除数量")
    private Integer deletedCount;

    @ApiModelProperty("因重新被引用而跳过的数量")
    private Integer skippedCount;

    @ApiModelProperty("释放字节数")
    private Long releasedBytes;
}
