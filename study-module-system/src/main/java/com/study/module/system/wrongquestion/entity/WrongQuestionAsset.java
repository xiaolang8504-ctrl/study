package com.study.module.system.wrongquestion.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 错题有序素材。 */
@Data
@TableName("sys_wrong_question_asset")
public class WrongQuestionAsset {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long wrongQuestionId;
    private Long userId;
    private String assetType;
    private Integer fileId;
    private String uploadType;
    private String imageUrl;
    private String label;
    private Integer sortNo;
    private Integer leftPosition;
    private Integer topPosition;
    private Integer width;
    private Integer height;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
