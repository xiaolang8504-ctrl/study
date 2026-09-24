package com.study.module.system.wrongquestion.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/** 批量整理学生自己的错题。字段为 null 时不修改，标签空数组表示清空标签。 */
@Data
public class WrongQuestionBatchOrganizeReq {

    @NotEmpty(message = "请选择错题")
    @Size(max = 200, message = "每次最多整理200道错题")
    private List<@NotNull Long> ids;

    @ApiModelProperty("教材版本")
    @Size(max = 100, message = "教材版本不能超过100个字符")
    private String textbookVersion;

    @ApiModelProperty("章节")
    @Size(max = 150, message = "章节不能超过150个字符")
    private String chapterName;

    @ApiModelProperty("个人标签，空数组表示清空")
    @Size(max = 20, message = "每道错题最多20个个人标签")
    private List<@Size(max = 30, message = "标签不能超过30个字符") String> tagNames;

    @ApiModelProperty("收藏：true收藏，false取消收藏")
    private Boolean favorite;

    @ApiModelProperty("优先级：0普通，1-5逐步提高")
    private Integer priorityLevel;

    @ApiModelProperty("标准知识点，空数组表示清空")
    @Size(max = 30, message = "每道错题最多关联30个知识点")
    private List<Long> knowledgePointIds;

    @ApiModelProperty("是否批量归档")
    private Boolean archive;
}
