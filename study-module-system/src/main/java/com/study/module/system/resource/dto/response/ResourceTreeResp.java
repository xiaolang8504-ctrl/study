package com.study.module.system.resource.dto.response;

import lombok.Data;

import java.util.List;

/**
 * API资源树形结构响应
 */
@Data
public class ResourceTreeResp {

    private Integer id;

    private String code;

    private String resourceName;

    private Integer pid;

    private Integer sort;

    private Boolean isSon;

    private List<ResourceTreeResp> children;
}
