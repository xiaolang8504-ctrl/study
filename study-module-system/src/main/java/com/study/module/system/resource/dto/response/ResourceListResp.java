package com.study.module.system.resource.dto.response;

import lombok.Data;

@Data
public class ResourceListResp {

    /**
     * 新增ID
     */
    private Integer id;

    /**
     * API编码
     */
    private String code;

    /**
     * API名称
     */
    private String resourceName;

    /**
     * API父ID
     */
    private Integer pid;

    /**
     * 排序号
     */
    private Integer sort;

    /**
     * 是否有子(1有,0无)
     */
    private Integer isSon;
}