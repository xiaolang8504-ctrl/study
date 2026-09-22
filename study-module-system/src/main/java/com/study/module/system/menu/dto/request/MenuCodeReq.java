package com.study.module.system.menu.dto.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 前端菜单编码定义
 */
@Data
public class MenuCodeReq {

    /**
     * 菜单编码
     */
    @NotBlank(message = "菜单编码不能为空")
    private String code;

    /**
     * 菜单名称
     */
    @NotBlank(message = "菜单名称不能为空")
    private String name;

    /**
     * 菜单层级
     */
    @NotNull(message = "菜单层级不能为空")
    private Integer level;

    /**
     * 子菜单
     */
    @Valid
    private List<MenuCodeReq> children;
}
