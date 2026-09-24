package com.study.module.system.review.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 练习题在在线作答、打印及文档导出中共用的图片素材。
 */
@Data
public class PracticeQuestionImageResp {

    @ApiModelProperty("文件服务ID；外部图片时为空")
    private Integer fileId;

    @ApiModelProperty("文件上传类型")
    private String uploadType;

    @ApiModelProperty("兼容外部图片地址")
    private String imageUrl;

    @ApiModelProperty("素材类型：ORIGINAL原图，GRAYSCALE灰度预览，QUESTION题目图片")
    private String imageType;

    @ApiModelProperty("归一化裁剪左坐标0-10000")
    private Integer leftPosition;

    @ApiModelProperty("归一化裁剪上坐标0-10000")
    private Integer topPosition;

    @ApiModelProperty("归一化裁剪宽度0-10000")
    private Integer width;

    @ApiModelProperty("归一化裁剪高度0-10000")
    private Integer height;
}
