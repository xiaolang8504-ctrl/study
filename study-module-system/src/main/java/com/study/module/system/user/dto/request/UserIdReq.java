package com.study.module.system.user.dto.request;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class UserIdReq {

    /**
     * 用户ID
     */
    @NotNull(message ="用户ID不能为空")
    private Long id;
}