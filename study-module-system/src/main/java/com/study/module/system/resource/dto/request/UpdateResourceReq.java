package com.study.module.system.resource.dto.request;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class UpdateResourceReq extends ResourceReq {

    /**
     * API资源Id
     */
    @NotNull(message ="API资源ID不能为空")
    private Integer id;
}