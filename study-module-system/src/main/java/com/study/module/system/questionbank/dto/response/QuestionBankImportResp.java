package com.study.module.system.questionbank.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * A4文件识别导入题库响应。
 */
@Data
public class QuestionBankImportResp {
    @ApiModelProperty("识别并导入的题目数量")
    private Integer importCount;
}
