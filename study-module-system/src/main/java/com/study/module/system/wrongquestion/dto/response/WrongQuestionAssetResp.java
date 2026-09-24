package com.study.module.system.wrongquestion.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/** 错题素材响应。 */
@Data
public class WrongQuestionAssetResp {

    @ApiModelProperty("素材ID")
    private Long id;
    @ApiModelProperty("素材类型")
    private String assetType;
    @ApiModelProperty("文件服务ID")
    private Integer fileId;
    @ApiModelProperty("文件上传类型")
    private String uploadType;
    @ApiModelProperty("外部图片地址")
    private String imageUrl;
    @ApiModelProperty("素材标签")
    private String label;
    @ApiModelProperty("素材顺序")
    private Integer sortNo;
    private Integer leftPosition;
    private Integer topPosition;
    private Integer width;
    private Integer height;
}
