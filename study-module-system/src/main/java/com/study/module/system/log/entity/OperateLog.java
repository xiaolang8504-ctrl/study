package com.study.module.system.log.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
public class OperateLog {

    /**
     * 操作日志ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 操作类型
     */
    private String operateType;

    /**
     * 操作类型文本
     */
    private String operateTypeText;

    /**
     * 操作所属模块文本
     */
    private String operateModuleText;

    /**
     * 操作内容
     */
    private String operateContent;

    /**
     * 操作人ID
     */
    private Long operateId;

    /**
     * 操作人名称
     */
    private String operateName;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;
}
