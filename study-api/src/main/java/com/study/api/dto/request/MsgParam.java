package com.study.api.dto.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息请求
 */
@Data
public class MsgParam implements Serializable {

    private static final long serialVersionUID = 8078740459954644512L;

    /**
     * 接收人
     */
    Long receiverId;

    /**
     * {}参数
     */
    Object[] params;

    public MsgParam() {
    }

    public MsgParam(Long receiverId, Object... params) {
        this.receiverId = receiverId;
        this.params = params;
    }
}
