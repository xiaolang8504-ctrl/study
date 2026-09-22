package com.study.module.system.msg.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息实体
 */
@Data
public class Msg {

    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * 消息类型文本
     */
    private String msgTypeText;

    /**
     * 消息标题
     */
    private String msgTitle;

    /**
     * 消息内容
     */
    private String msgContent;

    /**
     * 是否已读: 0否, 1是
     */
    private Integer isRead;

    /**
     * 消息接收人ID
     */
    private Long receiveId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
