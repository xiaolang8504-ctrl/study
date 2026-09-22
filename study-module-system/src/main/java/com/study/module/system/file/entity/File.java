package com.study.module.system.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件实体类
 */
@Data
public class File implements Serializable {

    private static final long serialVersionUID = -8597694025321602269L;

    /**
     * 文件ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 上传类型
     */
    private String uploadType;

    /**
     * 文件原名称
     */
    private String originName;

    /**
     * 文件保存新名称
     */
    private String saveName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件扩展名称
     */
    private String fileExtension;

    /**
     * 文件字节数
     */
    private Integer fileBytes;

    /**
     * 文件大小(格式化后)
     */
    private String fileSize;

    /**
     * 上传文件用户ID
     */
    private Long createId;

    /**
     * 上传时间
     */
    private LocalDateTime createTime;
}
