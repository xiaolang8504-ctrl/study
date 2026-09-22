package com.study.module.system.menu.dto.request;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class UpdateMenuReq extends MenuReq{

    /**
     * 菜单ID
     */
    @NotNull(message ="菜单ID不能为空")
    private Integer id;
}