package com.study.module.system.wrongquestion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.module.system.wrongquestion.dto.request.WrongQuestionDuplicateMergeReq;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionDuplicateResp;
import com.study.module.system.wrongquestion.dto.response.WrongQuestionOccurrenceResp;
import com.study.module.system.wrongquestion.entity.WrongQuestionDuplicateRelation;

import java.util.List;

/** 错题重复检测、来源归并和撤销服务。 */
public interface WrongQuestionDuplicateService extends IService<WrongQuestionDuplicateRelation> {
    List<WrongQuestionDuplicateResp> scanWrongQuestionDuplicate(Long wrongQuestionId);
    void mergeWrongQuestionDuplicate(WrongQuestionDuplicateMergeReq request);
    void undoWrongQuestionMerge(Long relationId);
    List<WrongQuestionOccurrenceResp> wrongQuestionOccurrenceList(Long wrongQuestionId);
}
