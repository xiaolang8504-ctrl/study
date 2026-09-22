package com.study.module.system.questionbank.dto.request;
import com.study.common.core.domain.dto.PageParam;
import lombok.Data;
/**
 * 相似题练习历史分页请求。
 */
@Data
public class QuestionPracticeHistoryPageListReq extends PageParam {
    private Integer isCorrect;
    private String experimentGroup;
}
