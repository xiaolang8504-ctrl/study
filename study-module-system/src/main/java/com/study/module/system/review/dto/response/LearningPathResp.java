package com.study.module.system.review.dto.response;

import lombok.Data;
import java.util.List;

/** 前置补缺、当前薄弱、间隔巩固组成的个人学习路径。 */
@Data
public class LearningPathResp {
    private String algorithmVersion;
    private String subject;
    private List<LearningPathTaskResp> taskList;
}
