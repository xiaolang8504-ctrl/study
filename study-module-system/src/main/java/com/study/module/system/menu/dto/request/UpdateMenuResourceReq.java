package com.study.module.system.menu.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 菜单关联API请求
 */
@Data
public class UpdateMenuResourceReq extends MenuIdReq {

    @ApiModelProperty(value = "已勾选API ID集", required = true)
    @NotNull(message = "API ID集不能为空")
    private List<Integer> resourceIds;

    @ApiModelProperty(value = "API层级ID集", required = true)
    @NotNull(message = "API层级ID集不能为空")
    private List<Integer> resourceLevelIds;
}
