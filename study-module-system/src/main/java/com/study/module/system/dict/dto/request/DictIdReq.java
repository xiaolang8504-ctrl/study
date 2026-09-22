package com.study.module.system.dict.dto.request;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * 字典ID请求类
 */
@Data
public class DictIdReq {

    @NotNull(message = "字典ID不为空")
    @Range(min = 1)
    private Integer id;
}
