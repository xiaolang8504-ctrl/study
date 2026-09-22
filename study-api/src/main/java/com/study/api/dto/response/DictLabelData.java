package com.study.api.dto.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典响应数据
 */
@Data
public class DictLabelData implements Serializable {

    private static final long serialVersionUID = 431637028271895831L;

    /**
     * 字典ID
     */
    private Integer id;

    /**
     * 字典键值
     */
    private String dictValue;

    /**
     * 字典标签
     */
    private String dictLabel;
}
