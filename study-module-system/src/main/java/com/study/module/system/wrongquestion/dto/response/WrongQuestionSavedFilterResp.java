package com.study.module.system.wrongquestion.dto.response;

import lombok.Data;

/** 已保存的错题筛选。 */
@Data
public class WrongQuestionSavedFilterResp {
    private Long id;
    private String filterName;
    private String filterJson;
    private Integer sortNo;
    private Integer defaultFlag;
}
